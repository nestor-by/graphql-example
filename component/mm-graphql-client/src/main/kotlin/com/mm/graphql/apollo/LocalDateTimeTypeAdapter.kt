package com.mm.graphql.apollo

import com.apollographql.apollo.response.CustomTypeAdapter
import com.apollographql.apollo.response.CustomTypeValue
import java.time.LocalDateTime

object LocalDateTimeTypeAdapter : CustomTypeAdapter<LocalDateTime> {
  override fun encode(value: LocalDateTime): CustomTypeValue<*> {
    return CustomTypeValue.GraphQLString(DateTimeHelper.toISOString(value))
  }

  override fun decode(value: CustomTypeValue<*>): LocalDateTime? {
    return DateTimeHelper.parseDate(value.value.toString())
  }
}

