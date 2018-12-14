package com.mm.graphql.dsl

import graphql.schema.*

fun newObject(name: String) = GraphQLObjectType.newObject().name(name)

inline fun <reified T : Enum<T>> newEnum(): GraphQLEnumType {
  return newEnum<T>(T::class.java.simpleName).build()
}

inline fun <reified T : Enum<T>> newEnum(name: String): GraphQLEnumType.Builder {
  return enumValues<T>()
      .fold(GraphQLEnumType.newEnum().name(name)) { a, b -> a.value(b.name) }
}

fun newInterface(name: String) = GraphQLInterfaceType.newInterface().name(name)

infix fun String.ofType(type_: GraphQLOutputType): GraphQLFieldDefinition.Builder {
  return GraphQLFieldDefinition.newFieldDefinition().type(type_).name(this)
}

operator fun String.rangeTo(type_: GraphQLOutputType) = this.ofType(type_)

fun GraphQLType.nonNull() = GraphQLNonNull(this)
operator fun GraphQLType.not() = GraphQLNonNull(this)

infix fun GraphQLFieldDefinition.Builder.staticValue(value: Any) = this.staticValue(value)
infix fun GraphQLFieldDefinition.Builder.description(d: String) = this.description(d)
infix fun <T> GraphQLFieldDefinition.Builder.dataFetcher(fetcher: DataFetcher<T>) = this.dataFetcher(fetcher)

fun listOfRefs(typeName: String) = GraphQLList(GraphQLTypeReference(typeName))
fun listOfObjs(wrappedType: GraphQLType) = GraphQLList(wrappedType)

infix fun GraphQLFieldDefinition.Builder.argument(builder: GraphQLArgument.Builder) = this.argument(builder)
infix fun GraphQLFieldDefinition.Builder.argument(newa: GQLArgumentBuilder) = this.argument(newa.build())

operator fun String.unaryPlus() = GQLArgumentBuilder(this)
operator fun GQLArgumentBuilder.rangeTo(type_: GraphQLInputType) = this.type(type_)

open class GQLArgumentBuilder(val name: String) {

  var type: GraphQLInputType? = null
  var defaultValue: Any? = null
  var description: String? = null


  infix fun description(description: String): GQLArgumentBuilder {
    this.description = description
    return this
  }


  infix fun type(type: GraphQLInputType): GQLArgumentBuilder {
    this.type = type
    return this
  }

  infix fun defaultValue(defaultValue: Any): GQLArgumentBuilder {
    this.defaultValue = defaultValue
    return this
  }

  fun build(): GraphQLArgument {
    return GraphQLArgument(name, description, type, defaultValue)
  }
}

