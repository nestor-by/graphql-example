package com.mm.graphql.apollo.reactor

import com.apollographql.apollo.ApolloCall
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.ApolloPrefetch
import com.apollographql.apollo.ApolloSubscriptionCall
import com.apollographql.apollo.api.Operation.Data
import com.apollographql.apollo.api.Operation.Variables
import com.apollographql.apollo.api.Query
import com.apollographql.apollo.api.Response
import com.apollographql.apollo.api.Subscription
import com.apollographql.apollo.api.cache.http.HttpCachePolicy
import com.apollographql.apollo.api.internal.Utils.checkNotNull
import com.apollographql.apollo.cache.normalized.ApolloStoreOperation
import com.apollographql.apollo.exception.ApolloException
import com.apollographql.apollo.internal.util.Cancelable
import reactor.core.Disposable
import reactor.core.Exceptions
import reactor.core.publisher.Flux
import reactor.core.publisher.FluxSink.OverflowStrategy
import reactor.core.publisher.Mono

fun ApolloClient.Builder.reactor(): ReactorClient {
  return ReactorClient(this.build())
}

class ReactorClient(private val client: ApolloClient) {

  fun <D : Data, T, V : Variables> watcher(query: Query<D, T, V>): Mono<Response<T>> {
    checkNotNull(query, "query == null")
    val watcher = client.query(query)
        .httpCachePolicy(HttpCachePolicy.CACHE_FIRST)
        .watcher()
    return Mono.create { emitter ->
      emitter.onCancel(createDisposable(watcher))

      watcher.enqueueAndWatch(object : ApolloCall.Callback<T>() {
        override fun onResponse(response: Response<T>) {
          emitter.success(response)
        }

        override fun onFailure(e: ApolloException) {
          Exceptions.throwIfFatal(e)
          emitter.error(e)
        }
      })
    }
  }

  fun <D : Data, T, V : Variables> from(query: Query<D, T, V>): Mono<Response<T>> {
    checkNotNull(query, "call == null")
    val call = client.query(query)
        .httpCachePolicy(HttpCachePolicy.CACHE_FIRST)
    return Mono.create { emitter ->
      emitter.onCancel(createDisposable(call))
      call.enqueue(object : ApolloCall.Callback<T>() {
        override fun onResponse(response: Response<T>) {
          emitter.success(response)
        }

        override fun onFailure(e: ApolloException) {
          Exceptions.throwIfFatal(e)
          emitter.error(e)
        }

        override fun onStatusEvent(event: ApolloCall.StatusEvent) {
          if (event == ApolloCall.StatusEvent.COMPLETED) {
            emitter.success()
          }
        }
      })
    }
  }

  fun <D : Data, Void, V : Variables> prefetch(query: Query<D, Void, V>): Mono<Response<Void>> {
    checkNotNull(query, "prefetch == null")
    val prefetch = client.prefetch(query)
    return Mono.create { emitter ->
      emitter.onCancel(createDisposable(prefetch))
      prefetch.enqueue(object : ApolloPrefetch.Callback() {
        override fun onSuccess() {
          emitter.success()
        }

        override fun onFailure(e: ApolloException) {
          Exceptions.throwIfFatal(e)
          emitter.error(e)
        }
      })
    }
  }

  fun <D : Data, T, V : Variables> from(query: Subscription<D, T, V>): Flux<Response<T>> {
    return from(query, OverflowStrategy.LATEST)
  }

  fun <D : Data, T, V : Variables> from(query: Subscription<D, T, V>, backpressureStrategy: OverflowStrategy): Flux<Response<T>> {
    checkNotNull(query, "originalCall == null")
    checkNotNull(backpressureStrategy, "backpressureStrategy == null")
    val call = client.subscribe(query)
    return Flux.create({ emitter ->
      emitter.onCancel(createDisposable(call))
      call.execute(
          object : ApolloSubscriptionCall.Callback<T> {
            override fun onResponse(response: Response<T>) {
              if (!emitter.isCancelled) {
                emitter.next(response)
              }
            }

            override fun onFailure(e: ApolloException) {
              Exceptions.throwIfFatal(e)
              if (!emitter.isCancelled) {
                emitter.error(e)
              }
            }

            override fun onCompleted() {
              if (!emitter.isCancelled) {
                emitter.complete()
              }
            }
          }
      )
    }, backpressureStrategy)
  }

  fun <T> from(operation: ApolloStoreOperation<T>): Mono<T> {
    checkNotNull(operation, "operation == null")
    return Mono.create { emitter ->
      operation.enqueue(object : ApolloStoreOperation.Callback<T> {
        override fun onSuccess(result: T) {
          emitter.success(result)
        }

        override fun onFailure(t: Throwable) {
          emitter.error(t)
        }
      })
    }
  }

  private fun createDisposable(cancelable: Cancelable): Disposable {
    return object : Disposable {
      override fun dispose() {
        cancelable.cancel()
      }

      override fun isDisposed(): Boolean {
        return cancelable.isCanceled
      }
    }
  }
}