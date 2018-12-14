package com.mm.graphql

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.mm.graphql.config.graphQlQueryModule
import com.mm.graphql.config.jooqModule
import com.mm.graphql.config.mongodbModule
import com.mm.graphql.params.QueryParam
import graphql.ExecutionInput
import graphql.GraphQL
import graphql.introspection.IntrospectionQuery.INTROSPECTION_QUERY
import io.netty.buffer.ByteBuf
import org.kodein.di.Kodein
import org.kodein.di.KodeinAware
import org.kodein.di.generic.instance
import reactor.core.publisher.Mono
import reactor.netty.DisposableServer
import reactor.netty.http.server.HttpServer
import reactor.netty.http.server.HttpServerRequest

object MainApp : KodeinAware {
  override val kodein = Kodein {
    import(jooqModule)
    import(mongodbModule)
    import(graphQlQueryModule)
  }
  private val graphQL: GraphQL by instance()

  private val mapper = jacksonObjectMapper()
      .registerModule(JavaTimeModule())
      .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
      .configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
      .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
      .setSerializationInclusion(JsonInclude.Include.NON_NULL)

  @JvmStatic
  fun main(args: Array<String>) {
    startDaemonAwaitThread(HttpServer
        .create()
        .port(8080)
        .route {
          it.get("/graphql") { _, resp ->
            val body = Mono.fromCallable {
              ExecutionInput.newExecutionInput()
                  .query(INTROSPECTION_QUERY)
                  .build()
            }.flatMap { input -> execute(graphQL, input) }
            resp.addHeader("Content-Type", "application/json")
            resp.sendString(body)
          }
          it.post("/graphql") { req, resp ->
            val body = getPayload(req)
                .map { payload ->
                  ExecutionInput.newExecutionInput()
                      .query(payload.query)
                      .variables(payload.variables)
                      .operationName(payload.operationName)
                      .build()
                }
                .flatMap { input -> execute(graphQL, input) }
            resp.addHeader("Content-Type", "application/json")
            resp.sendString(body)
          }
        }
        .wiretap(true)
        .bindNow())
  }

  private fun startDaemonAwaitThread(disposableServer: DisposableServer) {
    val awaitThread = object : Thread("server") {

      override fun run() {
        disposableServer.onDispose().block()
      }

    }
    awaitThread.contextClassLoader = javaClass.classLoader
    awaitThread.isDaemon = false
    awaitThread.start()
  }


  private fun execute(graphQL: GraphQL, input: ExecutionInput?): Mono<String?> {
    return Mono.fromFuture(graphQL.executeAsync(input))
        .map { executionResult ->
          val result = mutableMapOf<String, Any>()
          if (executionResult.errors.isNotEmpty()) {
            result["errors"] = executionResult.errors
          } else {
            result["data"] = executionResult.getData()
          }
          return@map mapper.writeValueAsString(result)
        }
  }

  private fun getPayload(request: HttpServerRequest): Mono<QueryParam> {
    return request.receiveContent()
        .map { it.content() }
        .map { bufferToBytes(it) }
        .reduce(ByteArray(0)) { t: ByteArray, u: ByteArray -> t + u }
        .map { buffer -> mapper.readValue(buffer, QueryParam::class.java) }
  }

  private fun bufferToBytes(buffer: ByteBuf): ByteArray {
    buffer.retain()
    try {
      val bytes = ByteArray(buffer.readableBytes())
      buffer.readBytes(bytes)
      return bytes
    } finally {
      buffer.release()
    }
  }
}


