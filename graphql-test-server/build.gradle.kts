import com.google.common.collect.ImmutableMap
import com.rohanprabhu.gradle.plugins.kdjooq.*
import com.rohanprabhu.gradle.plugins.kdjooq.jooqCodegenConfiguration
import org.gradle.api.internal.file.SourceDirectorySetFactory
import org.gradle.api.internal.tasks.DefaultSourceSet
import org.gradle.kotlin.dsl.support.serviceOf
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jooq.codegen.JavaGenerator
import org.jooq.meta.jaxb.CustomType
import org.jooq.meta.jaxb.Logging

plugins {
  id("com.rohanprabhu.kotlin-dsl-jooq")
}

jooqGenerator {
  jooqVersion = deps.versions.jooq
  configuration("default", sourceSet = sourceSets["main"]) {
    configuration = jooqCodegenConfiguration {
      jdbc = jdbc {
        username = "sa"
        password = ""
        driver = "org.h2.Driver"
        url = "jdbc:h2:mem:test;INIT=runscript from './src/main/resources/database.sql'"
      }

      logging = Logging.ERROR
      generator = generator {
        name = JavaGenerator::class.java.name
        database = database {
          includes = "language|author|book|book_store|book_to_book_store"
          forcedTypes = listOf(
              forcedType {
                name = "BOOLEAN"
                expression = ".*AUTHOR\\.DISTINGUISHED"
              }
          )
        }
        generate = generate {
          isPojos = false
          isDaos = false
          isFluentSetters = true
        }
        target = target {
          packageName = "com.mm.jooq"
          directory = "${project.buildDir}/generated/jooq/primary"
        }
      }
    }
  }
}

dependencies {
  compile(project(":component:mm-graphql-api"))
  compile(project(":component:mm-graphql-mongodb"))
  compile(project(":component:mm-graphql-jooq"))

  compile("com.h2database:h2:1.4.197")
  compile("com.zaxxer:HikariCP:3.2.0")

  compile(deps.graphql.core)
  compile(deps.kodein.core)
  compile(deps.reactor.core)
  compile(deps.reactor.netty)
  compile(deps.jackson.core)
  compile(deps.jackson.jsr310)
  compile(deps.logger.slf4j)
  compile(deps.logger.logback.core)
  compile(deps.logger.logback.classic)

  jooqGeneratorRuntime("com.h2database:h2:1.4.197")
}