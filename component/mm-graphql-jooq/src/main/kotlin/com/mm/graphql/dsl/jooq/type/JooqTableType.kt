package com.mm.graphql.dsl.jooq.type

import com.mm.graphql.dsl.camelCase
import com.mm.graphql.dsl.jooq.type.JooqFieldType
import graphql.Assert.assertNotNull
import graphql.PublicApi
import graphql.schema.*
import graphql.util.FpKit.valuesToList
import org.jooq.Record
import org.jooq.Table
import org.jooq.TableLike
import java.util.*

class JooqTableType<R : Record>(val table: Table<R>,
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

    fun build(): JooqTableType<R> {
      val fields = listOf(*table.fields())
          .filter { !fields.containsKey(it.name.camelCase()) }
          .fold(valuesToList(fields)) { obj, field -> obj + (JooqFieldType.Builder(field).build()) }
          .toList()
      val tableName = name ?: table.name.camelCase()
      return JooqTableType(table, tableName, description, fields, valuesToList(interfaces))
    }
  }

  companion object {
    fun <R : Record> tableQL(table: Table<R>): JooqTableType.Builder<R> = JooqTableType.Builder(table)
    fun <R : Record> tableQL(table: TableLike<R>): JooqTableType.Builder<R> = JooqTableType.Builder(table.asTable())
  }
}