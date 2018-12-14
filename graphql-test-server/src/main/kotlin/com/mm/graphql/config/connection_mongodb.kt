package com.mm.graphql.config

import com.mongodb.reactivestreams.client.MongoClient
import com.mongodb.reactivestreams.client.MongoClients
import org.kodein.di.Kodein
import org.kodein.di.generic.bind
import org.kodein.di.generic.instance
import org.kodein.di.generic.singleton

val mongodbModule = Kodein.Module("mongodbModule") {
  bind() from singleton { MongoClients.create() }
  bind() from singleton { instance<MongoClient>().getDatabase("idfinance") }
}
