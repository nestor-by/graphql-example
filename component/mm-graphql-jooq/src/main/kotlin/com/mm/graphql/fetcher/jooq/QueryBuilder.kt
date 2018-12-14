package com.mm.graphql.fetcher.jooq

import com.mm.graphql.dsl.jooq.relation.JooqRelationType
import com.mm.graphql.dsl.jooq.type.JooqArgumentType
import com.mm.graphql.dsl.jooq.type.JooqFieldType
import com.mm.graphql.dsl.jooq.type.JooqTableType
import graphql.schema.*
import org.jooq.*
import org.jooq.impl.DSL
import org.slf4j.LoggerFactory

class QueryBuilder(private val dslContext: DSLContext,
                   private val query: (env: DataFetchingEnvironment) -> Condition? = { null }) {
  private val logger = LoggerFactory.getLogger(QueryBuilder::class.java)

  fun build(environment: DataFetchingEnvironment): SelectConditionStep<*> {
    val from = dslContext // SelectWhereStep
        .select(getProjections(environment))
        .from(getTable(environment.fieldType))
    val conditions = (getQuery(environment) + query(environment)).filterNotNull()
    val query = getRelations(environment)
        .fold(from) { a, b -> a.join(b.getTable()).on(b.getCondition()) }
        .where(if (!conditions.isEmpty()) {
          conditions.reduce { a, b -> DSL.and(a, b) }
        } else {
          null
        })
    logger.info("Select jooq query:\n{}", query.sql)

    return query
  }


  private fun getQuery(environment: DataFetchingEnvironment): List<Condition> {
    val arguments = environment.arguments
    return getArguments(environment)
        .map { x -> x.function(arguments[x.name]!!) }
  }

  private fun getArguments(environment: DataFetchingEnvironment): List<JooqArgumentType<Any>> {
    val arguments = environment.arguments
    val haveArguments = arguments.isNotEmpty()
    val keys = arguments.keys
    return if (haveArguments) {
      environment.fieldDefinition
          .arguments
          .filter { x -> keys.contains(x.name) }
          .filterIsInstance<JooqArgumentType<Any>>()
    } else {
      emptyList()
    }
  }

  private fun getProjections(environment: DataFetchingEnvironment): List<Field<*>> {
    return environment
        .selectionSet.definitions
        .values
        .filterIsInstance<JooqFieldType<*>>()
        .map { it.field }
  }

  private fun getRelations(environment: DataFetchingEnvironment): List<JooqRelationType<*>> {
    val values = environment
        .selectionSet.definitions
        .values
    val queryTables = getArguments(environment).flatMap { getTable(it) }
    val queryRelations = getRelations(environment.fieldDefinition, queryTables)
    val projectionRelations = values.flatMap { listOf(it.type, it) + graphQLType(it) }

    return (projectionRelations + queryRelations)
        .toSet()
        .filterIsInstance<JooqRelationType<*>>()
  }

  private fun getRelations(definition: GraphQLFieldDefinition, tables: List<Table<out Record>>): List<JooqRelationType<*>> {
    val result = graphQLType(definition.type)
        .flatMap { type ->
          return@flatMap when (type) {
            is GraphQLObjectType -> type.fieldDefinitions.flatMap { getRelations(it, tables) }
            else -> emptyList()
          }
        }

    if (definition is JooqRelationType<*>) {
      if (tables.contains(definition.getTable()) || !result.isEmpty()) {
        return listOf<JooqRelationType<*>>(definition) + result
      }
    }
    return result
  }

  private fun getTable(fieldType: GraphQLType): List<Table<*>> {
    return graphQLType(fieldType).mapNotNull {
      val value = it
      return@mapNotNull when (value) {
        is JooqRelationType<*> -> value.getTable()
        is JooqTableType<*> -> value.table
        is JooqArgumentType<*> -> value.table
//        else -> throw IllegalArgumentException("Unsupported type $value")
        else -> null
      }
    }
  }


  private fun graphQLType(fieldType: GraphQLType): List<GraphQLType> {
    return when (fieldType) {
      is JooqRelationType<*> -> listOf(fieldType)
      is JooqArgumentType<*> -> listOf(fieldType)
      is GraphQLUnionType -> fieldType.types
      is GraphQLModifiedType -> graphQLType(fieldType.wrappedType)
      is GraphQLFieldDefinition -> graphQLType(fieldType.type)
      else -> listOf(fieldType)
    }
  }
}