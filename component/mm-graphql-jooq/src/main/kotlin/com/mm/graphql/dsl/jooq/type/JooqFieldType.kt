package com.mm.graphql.dsl.jooq.type

import com.mm.graphql.dsl.camelCase
import com.mm.graphql.fetcher.jooq.RecordDataFetcher
import com.mm.graphql.scalar.GraphQLLocalDate
import com.mm.graphql.scalar.GraphQLLocalDateTime
import com.mm.graphql.scalar.GraphQLLocalTime
import graphql.Assert.assertNotNull
import graphql.PublicApi
import graphql.Scalars
import graphql.Scalars.GraphQLBoolean
import graphql.schema.*
import graphql.schema.DataFetcherFactories.useDataFetcher
import graphql.util.FpKit.valuesToList
import org.jooq.Field
import org.jooq.impl.SQLDataType
import java.util.*

open class JooqFieldType<T>(
    val field: Field<T>,
    name: String,
    type: GraphQLOutputType,
    description: String?,
    dataFetcherFactory: DataFetcherFactory<*>,
    arguments: List<GraphQLArgument>,
    deprecationReason: String?)
  : GraphQLFieldDefinition(name, description, type, dataFetcherFactory, arguments, deprecationReason, emptyList(), null) {

  @PublicApi
  class Builder<T>(private val field: Field<T>) {

    private var name: String? = null
    private var type: GraphQLOutputType? = null
    private var description: String? = null
    private var dataFetcherFactory: DataFetcherFactory<*> = useDataFetcher(RecordDataFetcher(field))
    private var deprecationReason: String? = null
    private val arguments = LinkedHashMap<String, GraphQLArgument>()

    fun description(description: String): Builder<T> {
      this.description = description
      return this
    }

    fun type(type: GraphQLOutputType, converter: (v: T) -> Any): Builder<T> {
      this.type = type
      this.dataFetcherFactory = useDataFetcher(object : DataFetcher<Any> {
        val delegate = RecordDataFetcher(field)
        override fun get(env: DataFetchingEnvironment): Any? {
          return delegate.get(env)?.let { converter(it) }
        }
      })

      useDataFetcher(RecordDataFetcher(field))
      return this
    }

    fun dataFetcher(dataFetcher: DataFetcher<T>): Builder<T> {
      this.dataFetcherFactory = useDataFetcher<T>(dataFetcher)
      return this
    }

    fun dataFetcherFactory(dataFetcherFactory: DataFetcherFactory<T>): Builder<T> {
      assertNotNull(dataFetcherFactory, "dataFetcherFactory must be not null")
      this.dataFetcherFactory = dataFetcherFactory
      return this
    }

    fun staticValue(value: T): Builder<T> {
      this.dataFetcherFactory = useDataFetcher { value }
      return this
    }

    fun argument(argument: GraphQLArgument): Builder<T> {
      assertNotNull(argument, "argument can't be null")
      this.arguments[argument.name] = argument
      return this
    }

    fun argument(builder: GraphQLArgument.Builder) = argument(builder.build())

    fun argument(arguments: List<GraphQLArgument>): Builder<T> {
      assertNotNull(arguments, "arguments can't be null")
      for (argument in arguments) {
        argument(argument)
      }
      return this
    }

    fun deprecate(deprecationReason: String): Builder<T> {
      this.deprecationReason = deprecationReason
      return this
    }


    fun name(name: String?): Builder<T> {
      this.name = name;
      return this
    }

    fun build(): JooqFieldType<T> {
      return JooqFieldType(
          field,
          name ?: field.name.camelCase(),
          type ?: getType(field),
          description,
          dataFetcherFactory,
          valuesToList(arguments),
          deprecationReason
      )
    }

  }

  companion object {
    operator fun <T> Builder<T>.not() = GraphQLNonNull(this.build())

    fun <T> columnQL(field: Field<T>): Builder<T> = Builder(field)

    fun <T> getType(field: Field<T>): GraphQLOutputType {
      val dataType = field.dataType
      val type = when (dataType.typeName) {
        SQLDataType.NVARCHAR.typeName -> Scalars.GraphQLString
        SQLDataType.VARCHAR.typeName -> Scalars.GraphQLString
        SQLDataType.CLOB.typeName -> Scalars.GraphQLString
        SQLDataType.CHAR.typeName -> Scalars.GraphQLString
        SQLDataType.BOOLEAN.typeName -> Scalars.GraphQLBoolean
        SQLDataType.BIT.typeName -> GraphQLBoolean
        SQLDataType.TINYINT.typeName -> Scalars.GraphQLByte
        SQLDataType.SMALLINT.typeName -> Scalars.GraphQLShort
        SQLDataType.INTEGER.typeName -> Scalars.GraphQLInt
        SQLDataType.NUMERIC.typeName -> Scalars.GraphQLInt
        SQLDataType.BIGINT.typeName -> Scalars.GraphQLLong
        SQLDataType.DECIMAL_INTEGER.typeName -> Scalars.GraphQLBigInteger
        SQLDataType.DOUBLE.typeName -> Scalars.GraphQLFloat
        SQLDataType.REAL.typeName -> Scalars.GraphQLFloat
        SQLDataType.DECIMAL.typeName -> Scalars.GraphQLBigDecimal
        SQLDataType.DATE.typeName -> GraphQLLocalDate
        SQLDataType.TIMESTAMP.typeName -> GraphQLLocalDateTime
        SQLDataType.TIME.typeName -> GraphQLLocalTime
        SQLDataType.LOCALDATE.typeName -> GraphQLLocalDate
        SQLDataType.LOCALTIME.typeName -> GraphQLLocalTime
        SQLDataType.LOCALDATETIME.typeName -> GraphQLLocalDateTime
        else -> throw IllegalArgumentException("Unknown type $dataType for $field")
      }
      return if (!dataType.nullable()) GraphQLNonNull(type) else type
    }
  }
}