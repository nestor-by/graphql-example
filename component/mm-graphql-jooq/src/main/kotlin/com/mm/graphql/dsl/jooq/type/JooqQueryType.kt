package com.mm.graphql.dsl.jooq.type

import com.mm.graphql.dsl.camelCase
import com.mm.graphql.dsl.jooq.type.JooqFieldType
import graphql.Assert.assertNotNull
import graphql.PublicApi
import graphql.schema.*
import graphql.util.FpKit.valuesToList
import org.jooq.Record
import org.jooq.SelectConditionStep
import org.jooq.Table
import java.util.*

class JooqQueryType<R : Record>(val table: Table<R>,
                                name: String?,
                                description: String?,
                                fieldDefinitions: List<GraphQLFieldDefinition>,
                                interfaces: List<GraphQLOutputType>)
  : GraphQLObjectType(name, description, fieldDefinitions, interfaces) {

  @PublicApi
  class Builder<R : Record>(private val table: Table<R>) {
    private var name: String? = null
    private var description: String? = null
    private val fields = LinkedHashMap<String, GraphQLFieldDefinition>()
    private val interfaces = LinkedHashMap<String, GraphQLOutputType>()

    fun name(name: String): Builder<R> {
      this.name = name
      return this
    }

    fun description(description: String): Builder<R> {
      this.description = description
      return this
    }

    fun field(fieldDefinition: GraphQLFieldDefinition): Builder<R> {
      assertNotNull(fieldDefinition, "fieldDefinition can't be null")
      this.fields[fieldDefinition.name] = fieldDefinition
      return this
    }

    fun field(builder: GraphQLFieldDefinition.Builder): Builder<R> {
      return field(builder.build())
    }

    fun fields(fieldDefinitions: List<GraphQLFieldDefinition>): Builder<R> {
      assertNotNull(fieldDefinitions, "fieldDefinitions can't be null")
      fieldDefinitions.forEach { this.field(it) }
      return this
    }

    fun withInterface(interfaceType: GraphQLInterfaceType): Builder<R> {
      assertNotNull(interfaceType, "interfaceType can't be null")
      this.interfaces[interfaceType.name] = interfaceType
      return this
    }

    fun withInterface(reference: GraphQLTypeReference): Builder<R> {
      assertNotNull(reference, "reference can't be null")
      this.interfaces[reference.name] = reference
      return this
    }

    fun withInterfaces(vararg interfaceType: GraphQLInterfaceType): Builder<R> {
      for (type in interfaceType) {
        withInterface(type)
      }
      return this
    }

    fun build(): JooqQueryType<R> {
      val fields = listOf(*table.fields())
          .fold(valuesToList(fields)) { obj, field -> obj + (JooqFieldType.Builder(field).build()) }
      val tableName = name ?: table.name.camelCase()
      return JooqQueryType(table, tableName, description, fields, valuesToList(interfaces))
    }
  }

  companion object {
    fun <R : Record> queryQL(select: SelectConditionStep<R>): JooqTableType.Builder<R> = JooqTableType.Builder(select.asTable())
  }
}