package com.mm.graphql.dsl.jooq.relation

import com.mm.graphql.dsl.jooq.type.JooqTableType
import graphql.PublicApi
import graphql.schema.GraphQLList
import org.jooq.Condition
import org.jooq.Record
import org.jooq.Table
import org.jooq.TableField

@PublicApi
class JooqJoinType<R : Record>(private var wrappedType: JooqTableType<R>,
                               private val condition: Condition)
  : GraphQLList(wrappedType), JooqRelationType<R> {
  override fun getCondition(): Condition = condition
  override fun getTable(): Table<R> = wrappedType.table

  override fun getWrappedType(): JooqTableType<R> {
    return wrappedType
  }

  companion object {
    fun <R : Record> join(wrappedType: JooqTableType<R>, condition: Condition): JooqJoinType<R> {
      return JooqJoinType(wrappedType, condition)
    }

    fun <R : Record, T> join(wrappedType: JooqTableType<R>, foreignKey: TableField<*, T>): JooqJoinType<R> {
      val primaryKey = wrappedType.table.primaryKey.fields.first() as TableField<*, T>
      return JooqJoinType(wrappedType, primaryKey.eq(foreignKey))
    }
  }
}
