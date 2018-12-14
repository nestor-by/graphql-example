package com.mm.graphql.fetcher.mongodb

import com.mm.graphql.fetcher.ReactorDataFetcher
import com.mongodb.client.model.Filters
import com.mongodb.client.model.Filters.and
import com.mongodb.client.model.Projections.fields
import com.mongodb.client.model.Projections.include
import com.mongodb.reactivestreams.client.MongoDatabase
import graphql.language.Field
import graphql.schema.DataFetchingEnvironment
import org.bson.BsonDocument
import org.bson.conversions.Bson
import org.slf4j.LoggerFactory
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

class ManyDataFetcher(database: MongoDatabase, name: String) : ReactorDataFetcher<List<Map<String, Any>>> {
  private val collection = database.getCollection(name)
  private val logger = LoggerFactory.getLogger(name)

  override fun async(environment: DataFetchingEnvironment): Mono<List<Map<String, Any>>> {
    val query = getQuery(environment)
    val projections = getProjections(environment)
    logger.info("Many mongodb query = {}, projections = {}", query, projections)
    return Flux
        .from(collection.find(query)
            .projection(projections)
            .limit(environment.getArgument(LIMIT_ARG) ?: DEFAULT_LIMIT)
            .skip(environment.getArgument(OFFSET_ARG) ?: DEFAULT_OFFSET)
        )
        .map { it.toMap() }
        .collectList()
  }

  private fun getProjections(environment: DataFetchingEnvironment): Bson {
    val mapping = environment.selectionSet.get().filter { (k, _) -> !k.contains('/') }
    val fieldNames = projections(mapping.values.flatten())
    return fields(include(fieldNames))
  }

  private fun getQuery(environment: DataFetchingEnvironment): Bson? {
    val arguments = environment.arguments.filter { (a, _) -> !ARGS.contains(a) }
    val haveArguments = arguments.isNotEmpty()
    return if (haveArguments) {
      and(arguments.map { (k, v) -> Filters.eq(k, v) })
    } else {
      BsonDocument()
    }
  }

  private fun projections(value: List<Field>?): List<String> {
    return if (value?.isNotEmpty() == true) {
      value.flatMap { field ->
        val fieldName = field.name
        field.selectionSet?.selections?.let {
          projections(it.filterIsInstance<Field>()).map { x -> "$fieldName.$x" }
        } ?: listOf(fieldName).filter { !it.startsWith("__") }
      }
    } else emptyList()
  }

  companion object {
    private const val LIMIT_ARG: String = "limit"
    private const val OFFSET_ARG: String = "offset"
    private const val DEFAULT_LIMIT: Int = 10
    private const val DEFAULT_OFFSET: Int = 0
    private val ARGS = listOf(LIMIT_ARG, OFFSET_ARG)
  }
}