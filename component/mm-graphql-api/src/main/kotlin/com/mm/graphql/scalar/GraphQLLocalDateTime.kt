package com.mm.graphql.scalar

import graphql.language.StringValue
import graphql.schema.Coercing
import graphql.schema.CoercingParseValueException
import graphql.schema.CoercingSerializeException
import graphql.schema.GraphQLScalarType
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*


object GraphQLLocalDateTime : GraphQLScalarType("LocalDateTime", "Local Date Time type", object : Coercing<LocalDateTime, String> {
  private fun convertImpl(input: Any): LocalDateTime? {
    return when (input) {
      is String -> DateTimeHelper.parseDate(input)
      is Double -> Instant.ofEpochMilli(input.toLong())
          .atZone(ZoneId.systemDefault())
          .toLocalDateTime()
      is Date -> Instant.ofEpochMilli(input.time)
          .atZone(ZoneId.systemDefault())
          .toLocalDateTime()
      else -> null
    }
  }

  override fun serialize(input: Any): String {
    return DateTimeHelper.toISOString(if (input is LocalDateTime) {
      input
    } else {
      convertImpl(input) ?: throw CoercingSerializeException("Invalid value '$input' for LocalDate")
    })
  }

  override fun parseValue(input: Any): LocalDateTime {
    return convertImpl(input) ?: throw CoercingParseValueException("Invalid value '$input' for LocalDate")
  }

  override fun parseLiteral(input: Any): LocalDateTime? {
    if (input !is StringValue) return null
    return convertImpl(input.value)
  }
})
