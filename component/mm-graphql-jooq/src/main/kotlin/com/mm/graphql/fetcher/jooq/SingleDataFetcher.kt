package com.mm.graphql.fetcher.jooq

import com.mm.graphql.dsl.jooq.type.JooqTableType
import com.mm.graphql.fetcher.ReactorDataFetcher
import graphql.schema.DataFetchingEnvironment
import graphql.schema.GraphQLModifiedType
import graphql.schema.GraphQLType
import org.jooq.Condition
import org.jooq.Record
import org.jooq.Table
import org.jooq.TableField
import reactor.core.publisher.Mono

class SingleDataFetcher(private val queryBuilder: QueryBuilder) : ReactorDataFetcher<Record> {

  override fun async(environment: DataFetchingEnvironment): Mono<Record> {
    return Mono.justOrEmpty(queryBuilder.build(environment)
        .limit(DEFAULT_LIMIT)
        .offset(DEFAULT_OFFSET)
        .fetchOne())
  }

  private fun <T> getCondition(environment: DataFetchingEnvironment): Condition? {
    val primaryKey = getTable(environment.fieldType).primaryKey.fields.first() as TableField<*, T>
    val id: T? = environment.getArgument(SingleDataFetcher.ID_ARG)
    return id.let { primaryKey.eq(it) } ?: null
  }

  private fun getTable(fieldType: GraphQLType): Table<*> {
    val value = graphQLOutputType(fieldType)
    return when (value) {
      is JooqTableType<*> -> value.table
      else -> throw IllegalArgumentException("Unsupported type $value")
    }
  }


  private fun graphQLOutputType(fieldType: GraphQLType): GraphQLType {
    return when (fieldType) {
      is GraphQLModifiedType -> graphQLOutputType(fieldType.wrappedType)
      else -> fieldType
    }
  }

  companion object {
    private const val ID_ARG: String = "id"
    private const val DEFAULT_LIMIT: Int = 1
    private const val DEFAULT_OFFSET: Int = 0
  }
}