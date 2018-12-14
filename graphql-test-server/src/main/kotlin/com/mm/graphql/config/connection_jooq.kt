package com.mm.graphql.config

import com.zaxxer.hikari.HikariDataSource
import org.jooq.ConnectionProvider
import org.jooq.SQLDialect
import org.jooq.conf.MappedSchema
import org.jooq.conf.RenderMapping
import org.jooq.conf.Settings
import org.jooq.impl.DataSourceConnectionProvider
import org.jooq.impl.DefaultConfiguration
import org.jooq.impl.DefaultDSLContext
import org.jooq.impl.NoTransactionProvider
import org.kodein.di.Kodein
import org.kodein.di.generic.bind
import org.kodein.di.generic.instance
import org.kodein.di.generic.singleton
import org.slf4j.LoggerFactory


val jooqModule = Kodein.Module("jooqModule") {
  val log = LoggerFactory.getLogger("jooqModule")

  bind() from singleton {
    val source = HikariDataSource()
    source.jdbcUrl = "jdbc:h2:mem:test;INIT=runscript from 'classpath:database.sql'"
    source.username = "sa"
    source.password = ""
    source.maximumPoolSize = 5
    source.minimumIdle = 2
    source.idleTimeout = 20000
    source.maxLifetime = 30000
    source.connectionTimeout = 30000
    source.addDataSourceProperty("cachePrepStmts", true) //enable caching on driver side
    source.addDataSourceProperty("prepStmtCacheSize", 250)
    source.addDataSourceProperty("prepStmtCacheSqlLimit", 2048)

    log.info("Creating a connection pool for \"{}\", user \"{}\"...", source.jdbcUrl, source.username)

    source
  }
  bind() from singleton { DataSourceConnectionProvider(instance()) }
  bind("test") from singleton {
    val settings = Settings()
        .withRenderMapping(RenderMapping()
            .withSchemata(MappedSchema()
                .withInput("test")
                .withOutput("test")
            )
        )
        .withRenderFormatted(true)

    val configuration = DefaultConfiguration()
        .derive(instance<ConnectionProvider>())
        .derive(NoTransactionProvider())
//        .derive(*getProviders())
        .derive(SQLDialect.H2)
        .derive(settings)
    DefaultDSLContext(configuration)
  }
}