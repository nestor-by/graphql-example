package com.mm.graphql.fetcher

import graphql.ExceptionWhileDataFetching
import graphql.execution.DataFetcherResult
import graphql.schema.DataFetcher
import graphql.schema.DataFetchingEnvironment
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executors

interface ReactorDataFetcher<T> : DataFetcher<CompletableFuture<DataFetcherResult<T>>> {

  override fun get(environment: DataFetchingEnvironment): CompletableFuture<DataFetcherResult<T>> {
    return@get async(environment)
        .map { DataFetcherResult(it, emptyList()) }
        .onErrorResume { exception -> error(environment, exception) }
        .subscribeOn(Schedulers.fromExecutor(executorService))
        .toFuture()
  }

  fun error(environment: DataFetchingEnvironment, exception: Throwable?): Mono<DataFetcherResult<T>> {
    val path = environment.executionStepInfo.path
    val sourceLocation = environment.executionStepInfo.field.sourceLocation
    return Mono.just(DataFetcherResult<T>(null, listOf(
        ExceptionWhileDataFetching(path, exception, sourceLocation)
    )))
  }

  fun async(environment: DataFetchingEnvironment): Mono<T>

  companion object {
    private val executorThreadFactory = Executors.defaultThreadFactory()
    private val executorService = Executors.newFixedThreadPool(16, executorThreadFactory)
  }
}