package com.mm.graphql.scalar

import graphql.language.StringValue
import graphql.schema.Coercing
import graphql.schema.CoercingParseValueException
import graphql.schema.CoercingSerializeException
import graphql.schema.GraphQLScalarType
import java.time.Instant
import java.time.LocalTime
import java.time.ZoneId
import java.util.*


object GraphQLLocalTime : GraphQLScalarType("LocalTime", "Local Time type", object : Coercing<LocalTime, String> {
  private fun convertImpl(input: Any): LocalTime? {
    return when (input) {
      is String -> DateTimeHelper.parseDate(input)?.toLocalTime()
      is Double -> Instant.ofEpochMilli(input.toLong())
          .atZone(ZoneId.systemDefault())
          .toLocalTime()
      is Date -> Instant.ofEpochMilli(input.time)
          .atZone(ZoneId.systemDefault())
          .toLocalTime()
      else -> null
    }
  }

  override fun serialize(input: Any): String {
    return DateTimeHelper.toISOString(if (input is LocalTime) {
      input
    } else {
      convertImpl(input) ?: throw CoercingSerializeException("Invalid value '$input' for LocalDate")
    })
  }

  override fun parseValue(input: Any): LocalTime {
    return convertImpl(input) ?: throw CoercingParseValueException("Invalid value '$input' for LocalDate")
  }

  override fun parseLiteral(input: Any): LocalTime? {
    if (input !is StringValue) return null
    return convertImpl(input.value)
  }
})
