@file:Suppress("ClassName", "unused")

import org.gradle.api.Project
import java.io.File

fun String?.letIfEmpty(fallback: String): String {
  return if (this == null || isEmpty()) {
    fallback
  } else {
    this
  }
}

fun String?.execute(workingDir: File, fallback: String): String {
  Runtime.getRuntime().exec(this, null, workingDir).let {
    it.waitFor()
    return try {
      it.inputStream.reader().readText().trim().letIfEmpty(fallback)
    } catch (e: Exception) {
      fallback
    }
  }
}

/**
 * Round up to the nearest [multiple].
 *
 * Borrowed from https://gist.github.com/aslakhellesoy/1134482
 */
fun Int.roundUpToNearest(multiple: Int): Int {
  return if (this >= 0) (this + multiple - 1) / multiple * multiple else this / multiple * multiple
}

object build {
  val standardFreeKotlinCompilerArgs = listOf(
      "-Xjsr305=strict",
      "-progressive",
      "-XXLanguage:+NewInference",
      "-XXLanguage:+SamConversionForKotlinFunctions",
      "-XXLanguage:+InlineClasses"
  )
}

object deps {
  object versions {
    const val apollo = "1.0.0-alpha3"
    const val kotlin = "1.3.10"
    const val okhttp = "3.11.0"
    const val retrofit = "2.4.0"
    const val reactor = "3.2.2.RELEASE"
    const val reactorNetty = "0.8.2.RELEASE"
    const val jackson = "2.9.7"
    const val jooq = "3.11.7"
    const val kodein = "5.3.0"
    const val graphql = "11.0"
    const val jupiter = "5.3.1"
  }

  object apollo {
    const val gradlePlugin = "com.apollographql.apollo:apollo-gradle-plugin:${versions.apollo}"
    const val httpcache = "com.apollographql.apollo:apollo-http-cache:${versions.apollo}"
    const val runtime = "com.apollographql.apollo:apollo-runtime:${versions.apollo}"
    const val core = "com.apollographql.apollo:apollo-api:${versions.apollo}"
  }


  object build {
    val ci = "true" == System.getenv("CI")

    fun gitSha(project: Project): String {
      // query git for the SHA, Tag and commit count. Use these to automate versioning.
      return "git rev-parse --short HEAD".execute(project.rootDir, "none")
    }

    fun gitTag(project: Project): String {
      return "git describe --tags".execute(project.rootDir, "dev")
    }

    fun gitCommitCount(project: Project, isRelease: Boolean): Int {
      return 100 + ("git rev-list --count HEAD".execute(project.rootDir, "0").toInt().let {
        if (isRelease) it else it.roundUpToNearest(100)
      })
    }

    fun gitTimestamp(project: Project): Int {
      return "git log -n 1 --format=%at".execute(project.rootDir, "0").toInt()
    }

    object gradlePlugins {
      const val versions = "com.github.ben-manes:gradle-versions-plugin:0.17.0"
    }

    object repositories {
      const val google = "https://maven.google.com"
      const val jitpack = "https://jitpack.io"
      const val kotlineap = "https://dl.bintray.com/kotlin/kotlin-eap"
      const val kotlinx = "https://kotlin.bintray.com/kotlinx"
      const val plugins = "https://plugins.gradle.org/m2/"
      const val snapshots = "https://oss.sonatype.org/content/repositories/snapshots/"
    }

    const val javapoet = "com.squareup:javapoet:1.11.1"
  }


  object kotlin {
    private const val coroutinesVersion = "1.0.0"
    const val coroutines = "org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutinesVersion"
    const val gradlePlugin = "org.jetbrains.kotlin:kotlin-gradle-plugin:${versions.kotlin}"
    const val metadata = "me.eugeniomarletti.kotlin.metadata:kotlin-metadata:1.4.0"
    const val noArgGradlePlugin = "org.jetbrains.kotlin:kotlin-noarg:${versions.kotlin}"
    const val poet = "com.squareup:kotlinpoet:1.0.0-RC1"

    object stdlib {
      const val core = "org.jetbrains.kotlin:kotlin-stdlib:${versions.kotlin}"
      const val jdk7 = "org.jetbrains.kotlin:kotlin-stdlib-jdk7:${versions.kotlin}"
      const val jdk8 = "org.jetbrains.kotlin:kotlin-stdlib-jdk8:${versions.kotlin}"
    }
  }

  object misc {
    const val bugsnag = "com.bugsnag:bugsnag-android:4.8.2"

    object debug {
      const val flipper = "com.facebook.flipper:flipper:0.9.0"
      const val madge = "com.jakewharton.madge:madge:1.1.4"
      const val processPhoenix = "com.jakewharton:process-phoenix:2.0.0"
      const val scalpel = "com.jakewharton.scalpel:scalpel:1.1.2"
      const val telescope = "com.mattprecious.telescope:telescope:2.1.0"
    }

    const val flick = "me.saket:flick:1.4.0"
    const val gestureViews = "com.alexvasilkov:gesture-views:2.2.0"
    const val inboxRecyclerView = "me.saket:inboxrecyclerview:1.0.0-rc1"
    const val javaxInject = "org.glassfish:javax.annotation:10.0-b28"
    const val jsoup = "org.jsoup:jsoup:1.11.3"
    const val jsr305 = "com.google.code.findbugs:jsr305:3.0.2"
    const val lazythreeten = "com.gabrielittner.threetenbp:lazythreetenbp:0.4.0"
    const val lottie = "com.airbnb.android:lottie:2.8.0"
    const val moshiLazyAdapters = "com.serjltt.moshi:moshi-lazy-adapters:2.2"
    const val okio = "com.squareup.okio:okio:2.1.0"
    const val recyclerViewAnimators = "jp.wasabeef:recyclerview-animators:2.3.0"
    const val tapTargetView = "com.getkeepsafe.taptargetview:taptargetview:1.12.0"
    const val timber = "com.jakewharton.timber:timber:4.7.1"
    const val unbescape = "org.unbescape:unbescape:1.1.6.RELEASE"
  }

  object okhttp {
    const val core = "com.squareup.okhttp3:okhttp:${versions.okhttp}"

    object debug {
      const val loggingInterceptor = "com.squareup.okhttp3:logging-interceptor:${versions.okhttp}"
    }

    const val webSockets = "com.squareup.okhttp3:okhttp-ws:3.4.2"
  }


  object logger {
    const val slf4jVersion = "1.7.25"
    const val logbackVersion = "1.2.3"
    const val slf4j = "org.slf4j:slf4j-api:$slf4jVersion"

    object logback {
      const val core = "ch.qos.logback:logback-core:$logbackVersion"
      const val classic = "ch.qos.logback:logback-classic:$logbackVersion"
    }
  }

  object jackson {
    const val core =   "com.fasterxml.jackson.module:jackson-module-kotlin:${versions.jackson}"
    const val jsr310 = "com.fasterxml.jackson.datatype:jackson-datatype-jsr310:${versions.jackson}"
  }

  object jooq {
    const val core = "org.jooq:jooq:${versions.jooq}"
    const val gradlePlugin = "com.rohanprabhu.kotlin-dsl-jooq:com.rohanprabhu.kotlin-dsl-jooq.gradle.plugin:0.3.1"
  }

  object kodein {
    const val core = "org.kodein.di:kodein-di-generic-jvm:${versions.kodein}"
  }

  object graphql {
    const val core = "com.graphql-java:graphql-java:${versions.graphql}"
    const val extendedScalars = "com.graphql-java:graphql-java-extended-scalars:1.0"
  }

  object reactor {
    const val core = "io.projectreactor:reactor-core:${versions.reactor}"
    const val netty = "io.projectreactor.netty:reactor-netty:${versions.reactorNetty}"
  }

  object mongodb {
    const val async = "org.mongodb:mongodb-driver-reactivestreams:1.10.0"
  }

  object retrofit {
    const val core = "com.squareup.retrofit2:retrofit:${versions.retrofit}"

    object debug {
      const val mock = "com.squareup.retrofit2:retrofit-mock:${versions.retrofit}"
    }

    const val moshi = "com.squareup.retrofit2:converter-moshi:${versions.retrofit}"
    const val rxJava2 = "com.squareup.retrofit2:adapter-rxjava2:${versions.retrofit}"
    const val coroutines = "com.jakewharton.retrofit:retrofit2-kotlin-coroutines-adapter:0.9.2"
  }


  object test {
    object java {
      object jupiter {
        const val api = "org.junit.jupiter:junit-jupiter-api:${versions.jupiter}"
        const val params = "org.junit.jupiter:junit-jupiter-params:${versions.jupiter}"
        const val engine = "org.junit.jupiter:junit-jupiter-engine:${versions.jupiter}"
        const val vintage = "org.junit.vintage:junit-vintage-engine:4.12"
      }
    }

    const val mockitp = "com.nhaarman:mockito-kotlin:1.5.0"
  }
}