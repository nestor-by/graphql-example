package com.mm.graphql.client

import by.mcs.graphql.api.BorrowerByIdQuery
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
        .from(BorrowerByIdQuery(300))
        .map { it.data() }
        .flatMapIterable { x -> x?.borrowers }
        .subscribe { println(it.work?.education) }
  }
}
