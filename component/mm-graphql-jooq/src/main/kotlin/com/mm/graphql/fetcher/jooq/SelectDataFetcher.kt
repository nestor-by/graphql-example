package com.mm.graphql.fetcher.jooq

import com.mm.graphql.fetcher.ReactorDataFetcher
import graphql.schema.DataFetchingEnvironment
import org.jooq.Record
import reactor.core.publisher.Mono

class SelectDataFetcher(private val queryBuilder: QueryBuilder) : ReactorDataFetcher<List<Record>> {
  override fun async(environment: DataFetchingEnvironment): Mono<List<Record>> {
    val limit = environment.getArgument(LIMIT_ARG) ?: DEFAULT_LIMIT
    val offset = environment.getArgument(OFFSET_ARG) ?: DEFAULT_OFFSET
    return Mono.fromCompletionStage(queryBuilder
        .build(environment)
        .limit(limit)
        .offset(offset)
        .fetchAsync())
  }

  companion object {
    private const val LIMIT_ARG: String = "limit"
    private const val OFFSET_ARG: String = "offset"
    private val ARGS = listOf(LIMIT_ARG, OFFSET_ARG)

    private const val DEFAULT_LIMIT: Int = 10
    private const val DEFAULT_OFFSET: Int = 0
  }
}