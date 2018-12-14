package com.mm.graphql.apollo

import com.apollographql.apollo.response.CustomTypeAdapter
import com.apollographql.apollo.response.CustomTypeValue

object LongTypeAdapter : CustomTypeAdapter<Long> {
  override fun encode(value: Long): CustomTypeValue<*> {
    return CustomTypeValue.GraphQLNumber(value)
  }

  override fun decode(value: CustomTypeValue<*>): Long? {
    return value.value.toString().toLong()
  }
}

