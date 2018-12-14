package com.mm.graphql.scalar

import java.time.*
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.*
import java.util.concurrent.CopyOnWriteArrayList


object DateTimeHelper {

  private val DATE_FORMATTERS = CopyOnWriteArrayList<DateTimeFormatter>()

  init {
    DATE_FORMATTERS.add(DateTimeFormatter.ISO_INSTANT.withZone(ZoneOffset.UTC))
    DATE_FORMATTERS.add(DateTimeFormatter.ISO_LOCAL_DATE_TIME.withZone(ZoneOffset.UTC))
    DATE_FORMATTERS.add(DateTimeFormatter.ISO_LOCAL_DATE.withZone(ZoneOffset.UTC))
  }

  // ISO_8601
  fun toISOString(dateTime: LocalDateTime): String {
    return DateTimeFormatter.ISO_INSTANT.format(ZonedDateTime.of(dateTime, ZoneOffset.UTC))
  }

  fun toISOString(date: LocalDate): String {
    return DateTimeFormatter.ISO_LOCAL_DATE.format(date)
  }

  fun toISOString(time: LocalTime): String {
    return DateTimeFormatter.ISO_LOCAL_TIME.format(time)
  }

  fun toLocalDateTime(date: Date): LocalDateTime {
    return date.toInstant().atZone(ZoneOffset.UTC).toLocalDateTime()
  }


  fun parseDate(date: String): LocalDateTime? {
    for (formatter in DATE_FORMATTERS) {
      try {
        return if (formatter == DATE_FORMATTERS[2]) {
          LocalDate.parse(date, formatter).atStartOfDay()
        } else {
          LocalDateTime.parse(date, formatter)
        }
      } catch (ignored: DateTimeParseException) {
      }
    }
    return null
  }
}

