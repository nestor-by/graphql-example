package com.mm.graphql.config

import com.mm.graphql.dsl.jooq.rangeTo
import com.mm.graphql.dsl.jooq.relation.JooqManyToOneType
import com.mm.graphql.dsl.jooq.type.JooqTableType
import com.mm.graphql.dsl.newObject
import com.mm.graphql.dsl.rangeTo
import com.mm.jooq.test.Tables
import com.mm.jooq.test.tables.records.BookRecord
import graphql.Scalars


val place = newObject("Place")
    .field("country"..Scalars.GraphQLString)
    .field("district"..Scalars.GraphQLString)
    .field("region"..Scalars.GraphQLString)
    .field("category"..Scalars.GraphQLString)
    .field("value"..Scalars.GraphQLString)
    .build()

val street = newObject("Street")
    .field("place"..place)
    .field("category"..Scalars.GraphQLString)
    .field("value"..Scalars.GraphQLString)
    .build()

val address = newObject("Address")
    .field("street"..street)
    .field("building"..Scalars.GraphQLString)
    .build()


val authorType = JooqTableType.tableQL(Tables.AUTHOR).build()

val languageType = JooqTableType.tableQL(Tables.LANGUAGE).build()

val bookType = JooqTableType.tableQL(Tables.BOOK)
    .field("author"..JooqManyToOneType.manyToOne(authorType, Tables.BOOK.AUTHOR_ID))
    .field("language"..JooqManyToOneType.manyToOne(languageType, Tables.BOOK.LANGUAGE_ID))
    .build()

val storeType = JooqTableType.tableQL(Tables.BOOK_STORE)
    .field(("address"..address).dataFetcher {
      val bookId = it.getSource<BookRecord>()
      bookId
    })
    .build()

val authorWithBooksType = JooqTableType.tableQL(Tables.AUTHOR)
    .name("authorWithBooksType")
    .field("books"..JooqManyToOneType.manyToOne(bookType, Tables.AUTHOR.ID.eq(Tables.BOOK.AUTHOR_ID)))
    .build()

val stockType = JooqTableType.tableQL(Tables.BOOK_TO_BOOK_STORE)
    .field("book"..JooqManyToOneType.manyToOne(bookType, Tables.BOOK_TO_BOOK_STORE.BOOK_ID))
    .field("store"..JooqManyToOneType.manyToOne(storeType, Tables.BOOK_STORE.NAME.eq(Tables.BOOK_TO_BOOK_STORE.NAME)))
    .build()
