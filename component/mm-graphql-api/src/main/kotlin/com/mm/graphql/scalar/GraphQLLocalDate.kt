package com.mm.graphql.scalar

import graphql.language.StringValue
import graphql.schema.Coercing
import graphql.schema.CoercingParseValueException
import graphql.schema.CoercingSerializeException
import graphql.schema.GraphQLScalarType
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.util.*


object GraphQLLocalDate : GraphQLScalarType("LocalDate", "Local Date type", object : Coercing<LocalDate, String> {
  private fun convertImpl(input: Any): LocalDate? {
    return when (input) {
      is String -> DateTimeHelper.parseDate(input)?.toLocalDate()
      is Double -> Instant.ofEpochMilli(input.toLong())
          .atZone(ZoneId.systemDefault())
          .toLocalDate()
      is Date -> Instant.ofEpochMilli(input.time)
          .atZone(ZoneId.systemDefault())
          .toLocalDate()
      else -> null
    }
  }

  override fun serialize(input: Any): String {
    return DateTimeHelper.toISOString(if (input is LocalDate) {
      input
    } else {
      convertImpl(input) ?: throw CoercingSerializeException("Invalid value '$input' for LocalDate")
    })
  }

  override fun parseValue(input: Any): LocalDate {
    return convertImpl(input) ?: throw CoercingParseValueException("Invalid value '$input' for LocalDate")
  }

  override fun parseLiteral(input: Any): LocalDate? {
    if (input !is StringValue) return null
    return convertImpl(input.value)
  }
})
