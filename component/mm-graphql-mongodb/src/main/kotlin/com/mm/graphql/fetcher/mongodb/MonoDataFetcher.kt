package com.mm.graphql.fetcher.mongodb

import com.mm.graphql.fetcher.ReactorDataFetcher
import com.mongodb.client.model.Filters
import com.mongodb.client.model.Filters.and
import com.mongodb.client.model.Filters.eq
import com.mongodb.client.model.Projections.fields
import com.mongodb.client.model.Projections.include
import com.mongodb.reactivestreams.client.MongoDatabase
import graphql.schema.DataFetchingEnvironment
import org.bson.BsonDocument
import org.slf4j.LoggerFactory
import reactor.core.publisher.Mono

class MonoDataFetcher(database: MongoDatabase, name: String) : ReactorDataFetcher<Map<String, Any>> {
  private val collection = database.getCollection(name)
  private val logger = LoggerFactory.getLogger(name)

  override fun async(environment: DataFetchingEnvironment): Mono<Map<String, Any>> {
    val haveArguments = environment.arguments.isNotEmpty()
    val query = if (haveArguments) {
      environment.arguments
          .map(Filters::eq)
          .reduce { a, b -> and(a, b) }
    } else {
      BsonDocument()
    }

    val projections = environment.selectionSet.get()?.keys?.let { x ->
      fields(include(x.toList()))
    } ?: BsonDocument()

    logger.info("Mono mongodb query = {}, projections = {}", query, projections)
    return Mono
        .from(collection.find(query).projection(projections))
        .map { it.toMap() }
  }
}