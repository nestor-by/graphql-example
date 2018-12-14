package com.mm.graphql.dsl.jooq

import com.mm.graphql.dsl.argument
import com.mm.graphql.dsl.jooq.relation.JooqManyToOneType
import com.mm.graphql.dsl.jooq.type.JooqArgumentType
import com.mm.graphql.dsl.rangeTo
import com.mm.graphql.dsl.unaryPlus
import com.mm.graphql.fetcher.jooq.QueryBuilder
import com.mm.graphql.fetcher.jooq.SelectDataFetcher
import com.mm.graphql.fetcher.jooq.SingleDataFetcher
import graphql.Scalars
import graphql.schema.GraphQLFieldDefinition
import org.jooq.Condition
import org.jooq.DSLContext
import org.jooq.Record
import org.jooq.TableField

infix fun GraphQLFieldDefinition.Builder.dataFetcher(condition: Condition): GraphQLFieldDefinition.Builder {
  return this
}

infix fun <R : Record, T> GraphQLFieldDefinition.Builder.dataFetcher(field: TableField<R, T>): GraphQLFieldDefinition.Builder {
  return this
}

operator fun String.rangeTo(type_: JooqManyToOneType.Builder<*>) = type_.name(this).build()

infix fun GraphQLFieldDefinition.Builder.argument(newa: JooqArgumentType.Builder<*>) = this.argument(newa.build())

infix fun GraphQLFieldDefinition.Builder.list(dslContext: DSLContext): GraphQLFieldDefinition.Builder {
  return this
      .argument(+"limit"..Scalars.GraphQLInt description " limit")
      .argument(+"offset"..Scalars.GraphQLInt description " offset")
      .dataFetcher(SelectDataFetcher(QueryBuilder(dslContext)));
}


infix fun GraphQLFieldDefinition.Builder.single(dslContext: DSLContext): GraphQLFieldDefinition.Builder {
  return this.dataFetcher(SingleDataFetcher(QueryBuilder(dslContext)));
}
