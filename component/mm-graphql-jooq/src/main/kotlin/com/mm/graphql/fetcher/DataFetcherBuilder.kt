package com.mm.graphql.fetcher

import com.mm.graphql.fetcher.jooq.QueryBuilder
import com.mm.graphql.fetcher.jooq.SelectDataFetcher
import com.mm.graphql.fetcher.jooq.SingleDataFetcher
import graphql.schema.DataFetchingEnvironment
import org.jooq.Condition
import org.jooq.DSLContext
import org.jooq.Record

class DataFetcherBuilder(private val dslContext: DSLContext) {

  fun single(query: (env: DataFetchingEnvironment) -> Condition): ReactorDataFetcher<Record> {
    return SingleDataFetcher(QueryBuilder(dslContext, query))
  }

  fun single(): ReactorDataFetcher<Record> {
    return SingleDataFetcher(QueryBuilder(dslContext))
  }

  fun list(): ReactorDataFetcher<List<Record>> {
    return SelectDataFetcher(QueryBuilder(dslContext))
  }
}