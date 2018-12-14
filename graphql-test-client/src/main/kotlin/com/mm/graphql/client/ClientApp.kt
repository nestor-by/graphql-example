package com.mm.graphql.client

import com.idfinance.graphql.api.FinBooksCountQuery
import com.mm.graphql.apollo.reactor.ReactorClient
import com.mm.graphql.client.config.clientModule
import org.kodein.di.Kodein
import org.kodein.di.KodeinAware
import org.kodein.di.generic.instance

object ClientApp : KodeinAware {
  override val kodein = Kodein {
    import(clientModule)
  }

  private val client: ReactorClient by instance()

  @JvmStatic
  fun main(args: Array<String>) {
    client
        .from(FinBooksCountQuery(listOf(1, 2, 3)))
        .map { it.data() }
        .flatMapIterable { x -> x?.finBooksCount }
        .subscribe {
          println(it.book?.author?.lastName)
          println(it.book?.title)
          println("~~~~~~~~~")
        }
  }
}
