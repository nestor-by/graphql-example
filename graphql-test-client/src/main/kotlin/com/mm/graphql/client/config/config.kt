package com.mm.graphql.client.config

import com.apollographql.apollo.ApolloClient
import com.mm.graphql.apollo.reactor.reactor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.kodein.di.Kodein
import org.kodein.di.generic.bind
import org.kodein.di.generic.instance
import org.kodein.di.generic.singleton
import org.slf4j.LoggerFactory

val clientModule = Kodein.Module("clientModule") {
  bind() from singleton {
    val logger = LoggerFactory.getLogger(OkHttpClient::class.java)

    OkHttpClient.Builder()
        .addInterceptor(HttpLoggingInterceptor { logger.debug(it) }.setLevel(HttpLoggingInterceptor.Level.BODY))
        .build()
  }
  bind() from singleton {
    val logger = LoggerFactory.getLogger(ApolloClient::class.java)

    ApolloClient.builder()
        .logger { priority, message, _, params -> logger.info("{}: {}", priority, String.format(message, *params)) }
        .serverUrl("http://localhost:8080/graphql")
        .okHttpClient(instance())
//        .addCustomTypeAdapter(CustomType.LOCALDATE, LocalDateTypeAdapter)
//        .addCustomTypeAdapter(CustomType.LOCALDATETIME, LocalDateTimeTypeAdapter)
        .reactor()
  }
}