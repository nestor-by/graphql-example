package com.mm.graphql.dsl.jooq.relation


import com.mm.graphql.dsl.jooq.type.JooqTableType
import com.mm.graphql.fetcher.ProvidedDataFetcher
import graphql.Assert
import graphql.PublicApi
import graphql.schema.*
import graphql.util.FpKit.valuesToList
import org.jooq.Condition
import org.jooq.Record
import org.jooq.Table
import org.jooq.TableField
import java.util.*

@PublicApi
class JooqManyToOneType<R : Record>(
    name: String?,
    description: String?,
    private val type: JooqTableType<R>,
    private val condition: Condition,
    dataFetcherFactory: DataFetcherFactory<*>,
    arguments: List<GraphQLArgument>,
    deprecationReason: String?) : GraphQLFieldDefinition(
    name,
    description,
    type,
    dataFetcherFactory,
    arguments,
    deprecationReason, emptyList(), null), JooqRelationType<R> {
  override fun getType(): JooqTableType<R> = type
  override fun getCondition() = condition
  override fun getTable(): Table<R> = type.table

  @PublicApi
  class Builder<R : Record>(private val type: JooqTableType<R>, private val condition: Condition) {
    private var name: String? = null

    private var description: String? = null
    private var dataFetcherFactory: DataFetcherFactory<R>? = null
    private var deprecationReason: String? = null
    private val arguments = LinkedHashMap<String, GraphQLArgument>()

    fun name(name: String): Builder<R> {
      this.name = name
      return this
    }

    fun description(description: String): Builder<R> {
      this.description = description
      return this
    }

    fun dataFetcher(dataFetcher: DataFetcher<R>): Builder<R> {
      Assert.assertNotNull(dataFetcher, "dataFetcher must be not null")
      this.dataFetcherFactory = DataFetcherFactories.useDataFetcher<R>(dataFetcher)
      return this
    }

    fun dataFetcherFactory(dataFetcherFactory: DataFetcherFactory<R>): Builder<R> {
      Assert.assertNotNull(dataFetcherFactory, "dataFetcherFactory must be not null")
      this.dataFetcherFactory = dataFetcherFactory
      return this
    }

    fun staticValue(value: R): Builder<R> {
      this.dataFetcherFactory = DataFetcherFactories.useDataFetcher { value }
      return this
    }

    fun argument(argument: GraphQLArgument): Builder<R> {
      Assert.assertNotNull(argument, "argument can't be null")
      this.arguments[argument.name] = argument
      return this
    }

    fun argument(builder: GraphQLArgument.Builder) = argument(builder.build())

    fun argument(arguments: List<GraphQLArgument>): Builder<R> {
      Assert.assertNotNull(arguments, "arguments can't be null")
      for (argument in arguments) {
        argument(argument)
      }
      return this
    }

    fun deprecate(deprecationReason: String): Builder<R> {
      this.deprecationReason = deprecationReason
      return this
    }

    fun build(): GraphQLFieldDefinition {
      return JooqManyToOneType(name,
          description,
          type,
          condition,
          dataFetcherFactory ?: DataFetcherFactories.useDataFetcher(ProvidedDataFetcher<Any>()),
          valuesToList(arguments),
          deprecationReason
      )
    }
  }

  companion object {

    fun <R : Record> manyToOne(wrappedType: JooqTableType.Builder<R>, condition: Condition): Builder<R> {
      return manyToOne(wrappedType.build(), condition)
    }

    fun <R : Record> manyToOne(wrappedType: JooqTableType<R>, condition: Condition): Builder<R> {
      return Builder(wrappedType, condition)
    }

    fun <R : Record, T> manyToOne(wrappedType: JooqTableType.Builder<R>, foreignKey: TableField<*, T>): Builder<R> {
      return manyToOne(wrappedType.build(), foreignKey)
    }

    fun <R : Record, T> manyToOne(wrappedType: JooqTableType<R>, foreignKey: TableField<*, T>): Builder<R> {
      val primaryKey = wrappedType.table.primaryKey.fields.first() as TableField<*, T>
      return Builder(wrappedType, primaryKey.eq(foreignKey))
    }
  }
}
