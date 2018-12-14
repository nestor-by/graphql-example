package com.mm.graphql.fetcher.jooq

import graphql.schema.DataFetcher
import graphql.schema.DataFetchingEnvironment
import graphql.schema.PropertyDataFetcher
import org.jooq.Field
import org.jooq.Record
import java.util.Objects.requireNonNull

class RecordDataFetcher<T>(field: Field<T>) : DataFetcher<T> {

  private val propertyName: Field<T> = requireNonNull(field)
  private val defaultDataFetcher: PropertyDataFetcher<T> = PropertyDataFetcher.fetching<T>(field.name)

  override fun get(env: DataFetchingEnvironment): T? {
    val source = env.getSource<Any>()
    return when (source) {
      is Record -> source[propertyName]
      else -> defaultDataFetcher[env]
    }
  }
}
