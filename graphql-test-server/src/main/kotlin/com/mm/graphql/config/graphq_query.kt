package com.mm.graphql.config

import com.mm.graphql.dsl.*
import com.mm.graphql.dsl.jooq.argument
import com.mm.graphql.dsl.jooq.list
import com.mm.graphql.dsl.jooq.rangeTo
import com.mm.graphql.dsl.jooq.relation.JooqManyToOneType
import com.mm.graphql.dsl.jooq.single
import com.mm.graphql.dsl.jooq.type.JooqArgumentType
import com.mm.graphql.dsl.jooq.type.JooqArgumentType.Companion.rangeTo
import com.mm.graphql.dsl.jooq.type.JooqTableType
import com.mm.graphql.fetcher.mongodb.ManyDataFetcher
import com.mm.jooq.test.Tables
import graphql.GraphQL
import graphql.Scalars
import graphql.execution.AsyncExecutionStrategy
import graphql.schema.GraphQLList
import graphql.schema.GraphQLSchema
import org.jooq.DSLContext
import org.jooq.impl.DSL
import org.kodein.di.Kodein
import org.kodein.di.generic.bind
import org.kodein.di.generic.instance
import org.kodein.di.generic.singleton

val graphQlQueryModule = Kodein.Module("graphQlQueryModule ") {
  bind() from singleton {
    try {
      val dslContext = instance<DSLContext>("test")
      val queryType = newObject("QueryType")
//Jooq query
          .field("findAllStocks"..GraphQLList.list(stockType)
              list dslContext
          )
          .field("findAllAuthors"..GraphQLList.list(authorWithBooksType)
              list dslContext
          )
          .field("findAllBooks"..GraphQLList.list(bookType)
              list dslContext
          )
          .field("getBookById"..GraphQLList.list(bookType)
              argument (+"id"..JooqArgumentType.refs(Tables.BOOK.ID) description "borrower id")
              single dslContext
          )
          .field("finBooksCount"..GraphQLList.list(JooqTableType.tableQL(
              dslContext
                  .select(
                      DSL.sum(Tables.BOOK_TO_BOOK_STORE.STOCK).`as`("STOCK_SUM"),
                      Tables.BOOK_TO_BOOK_STORE.BOOK_ID
                  )
                  .from(Tables.BOOK_TO_BOOK_STORE.join(Tables.BOOK).on(Tables.BOOK_TO_BOOK_STORE.BOOK_ID.eq(Tables.BOOK.ID)))
                  .where(Tables.BOOK.PUBLISHED_IN.gt(1800))
                  .groupBy(Tables.BOOK_TO_BOOK_STORE.BOOK_ID))
              .field("book"..JooqManyToOneType.Companion.manyToOne(bookType, Tables.BOOK.ID.eq(DSL.field("BOOK_ID", Int::class.java))))
              .build()
          )
              argument (+"ids"..JooqArgumentType.refs<Long>("BOOK_ID") description " book id")
              argument (+"date"..JooqArgumentType.refs<Int>("STOCK_SUM") { field, value -> field.between(value[0], value[1]) } description " stock sum")
              list (dslContext)
          )
//Mongodb query
          .field("subjects"..GraphQLList(address)
              argument (+"limit"..Scalars.GraphQLInt description " limit")
              argument (+"offset"..Scalars.GraphQLInt description " offset")
              dataFetcher ManyDataFetcher(instance(), "addresses"))


      val schema = GraphQLSchema
          .newSchema()
          .query(queryType)
          .build()

      return@singleton GraphQL.newGraphQL(schema)
          .queryExecutionStrategy(AsyncExecutionStrategy())
          .build()
    } catch (e: Exception) {
      throw IllegalArgumentException(e)
    }
  }
}