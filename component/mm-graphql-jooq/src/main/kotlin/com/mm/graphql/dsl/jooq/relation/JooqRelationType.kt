package com.mm.graphql.dsl.jooq.relation

import org.jooq.Condition
import org.jooq.Record
import org.jooq.Table

interface JooqRelationType<R : Record> {
  fun getCondition(): Condition
  fun getTable(): Table<R>
}