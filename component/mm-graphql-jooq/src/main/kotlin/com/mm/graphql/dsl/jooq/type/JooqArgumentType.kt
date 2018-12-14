package com.mm.graphql.dsl.jooq.type

import com.mm.graphql.dsl.GQLArgumentBuilder
import com.mm.graphql.scalar.GraphQLLocalDate
import com.mm.graphql.scalar.GraphQLLocalDateTime
import com.mm.graphql.scalar.GraphQLLocalTime
import graphql.Scalars
import graphql.language.InputValueDefinition
import graphql.schema.GraphQLArgument
import graphql.schema.GraphQLInputType
import graphql.schema.GraphQLList.list
import graphql.schema.GraphQLNonNull
import org.jooq.Condition
import org.jooq.Field
import org.jooq.Table
import org.jooq.TableField
import org.jooq.impl.DSL.field
import org.jooq.impl.SQLDataType

class JooqArgumentType<E>(name: String?,
                          type: GraphQLInputType,
                          val function: (x: E) -> org.jooq.Condition,
                          val table: Table<*>?,
                          description: String?,
                          defaultValue: Any?,
                          definition: InputValueDefinition?)
  : GraphQLArgument(name, description, type, defaultValue, definition) {

  fun condition(value: E): Condition {
    return function(value)
  }

  class Builder<E>(private val name: String, private val function: (x: E) -> org.jooq.Condition) {

    private var type: GraphQLInputType? = null
    private var table: Table<*>? = null
    private var defaultValue: Any? = null
    private var description: String? = null
    private var definition: InputValueDefinition? = null

    infix fun description(description: String): Builder<E> {
      this.description = description
      return this
    }

    infix fun table(table: Table<*>?): Builder<E> {
      this.table = table
      return this
    }

    infix fun definition(definition: InputValueDefinition): Builder<E> {
      this.definition = definition
      return this
    }

    infix fun type(type: GraphQLInputType?): Builder<E> {
      this.type = type
      return this
    }

    infix fun defaultValue(defaultValue: Any): Builder<E> {
      this.defaultValue = defaultValue
      return this
    }

    fun build(): JooqArgumentType<E> {
      return JooqArgumentType(name, type!!, function, table, description, defaultValue, definition)
    }
  }


  companion object {
    data class JooqArgumentData<E>(val type: GraphQLInputType, val table: Table<*>?, val function: (x: E) -> Condition)

    private fun <T> getType(field: Field<T>): GraphQLInputType {
      val dataType = field.dataType
      return when (dataType.typeName) {
        SQLDataType.NVARCHAR.typeName -> Scalars.GraphQLString
        SQLDataType.VARCHAR.typeName -> Scalars.GraphQLString
        SQLDataType.CHAR.typeName -> Scalars.GraphQLString
        SQLDataType.BOOLEAN.typeName -> Scalars.GraphQLBoolean
        SQLDataType.BIT.typeName -> Scalars.GraphQLBoolean
        SQLDataType.TINYINT.typeName -> Scalars.GraphQLByte
        SQLDataType.SMALLINT.typeName -> Scalars.GraphQLShort
        SQLDataType.INTEGER.typeName -> Scalars.GraphQLInt
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
    }

    operator fun <E> JooqArgumentData<E>.not() = JooqArgumentData(GraphQLNonNull(this.type), this.table, this.function)

    fun <T> ref(field: Field<T>): JooqArgumentData<T> {
      return ref(field, Field<T>::eq)
    }

    fun <T> refs(field: Field<T>, function: (v: Field<T>, x: List<T>) -> org.jooq.Condition): JooqArgumentData<List<T>> {
      val type = list(getType(field))
      val table = when (field) {
        is TableField<*, T> -> field.table
        else -> null
      }
      return JooqArgumentData(type, table) { x -> function(field, x) }
    }

    inline fun <reified T> ref(name: String): JooqArgumentData<T> {
      return ref(field(name, T::class.java))
    }

    inline fun <reified T> refs(name: String): JooqArgumentData<List<T>> {
      return refs(field(name, T::class.java))
    }

    inline fun <reified T> refs(name: String, noinline function: (v: Field<T>, x: List<T>) -> org.jooq.Condition): JooqArgumentData<List<T>> {
      return refs(field(name, T::class.java), function)
    }

    fun <T> refs(field: Field<T>): JooqArgumentData<List<T>> {
      return refs(field, Field<T>::`in`)
    }

    fun <T> ref(field: Field<T>, function: (v: Field<T>, x: T) -> org.jooq.Condition): JooqArgumentData<T> {
      val type = getType(field)
      val table = when (field) {
        is TableField<*, T> -> field.table
        else -> null
      }
      return JooqArgumentData(type, table) { x -> function(field, x) }
    }

    operator fun <E> GQLArgumentBuilder.rangeTo(data: JooqArgumentData<E>): JooqArgumentType.Builder<E> {
      var result = JooqArgumentType.Builder(this.name, data.function)
          .table(data.table)
          .type(data.type)
      this.defaultValue?.apply { result = result.defaultValue(this) }
      this.description?.apply { result = result.description(this) }
      return result
    }

    operator fun <T, E> GQLArgumentBuilder.invoke(function: (x: E) -> org.jooq.Condition): JooqArgumentType.Builder<E> {
      var result = JooqArgumentType.Builder(this.name, function)
          .type(type)
      this.defaultValue?.apply { result = result.defaultValue(this) }
      this.description?.apply { result = result.description(this) }
      return result
    }
  }
}