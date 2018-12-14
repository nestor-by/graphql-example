package com.mm.graphql.apollo

import com.apollographql.apollo.response.CustomTypeAdapter
import com.apollographql.apollo.response.CustomTypeValue
import java.time.LocalDate

object LocalDateTypeAdapter : CustomTypeAdapter<LocalDate> {
  override fun encode(value: LocalDate): CustomTypeValue<*> {
    return CustomTypeValue.GraphQLString(DateTimeHelper.toISOString(value))
  }

  override fun decode(value: CustomTypeValue<*>): LocalDate? {
    return DateTimeHelper.parseDate(value.value.toString())?.toLocalDate()
  }
}

