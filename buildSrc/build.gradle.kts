buildscript {
  repositories {
    jcenter()
  }
}

repositories {
  jcenter()
}

plugins {
  `kotlin-dsl`
  `java-gradle-plugin`
}


gradlePlugin {
  plugins {
    register("kotlinDslJooqPlugin") {
      id = "com.rohanprabhu.kotlin-dsl-jooq"
      implementationClass = "com.rohanprabhu.gradle.plugins.kdjooq.KotlinDslJooqPlugin"
    }
  }
}

dependencies {
  gradleApi()
  compile("org.jooq:jooq:3.11.7")
  compile("org.jooq:jooq-meta:3.11.7")
  compile("org.jooq:jooq-codegen:3.11.7")
}

kotlinDslPluginOptions {
  experimentalWarning.set(false)
}
