package com.mm.graphql.dsl


fun String.snakeCase(): String {
  val value = this
  val buffer = StringBuilder()
  buffer.append(Character.toLowerCase(value[0]))
  var count = 0
  for (i in 1 until value.length) {
    count++
    val ch = value[i]
    if (Character.isUpperCase(value[i - 1]) xor Character.isUpperCase(ch) && count > 1) {
      buffer.append('_')
      count = 0
    }
    buffer.append(Character.toLowerCase(ch))
  }
  return buffer.toString().replace("__", "_")
}

fun String.camelCase(): String {
  val value = this.replace("__", "_")
  val buffer = StringBuilder()
  buffer.append(Character.toLowerCase(value[0]))
  for (i in 1 until value.length) {
    val ch = value[i]
    if (ch != '_') {
      if (value[i - 1] == '_') {
        buffer.append(Character.toUpperCase(ch)).toList()
      } else {
        buffer.append(Character.toLowerCase(ch))
      }
    }
  }
  return buffer.toString()
}
