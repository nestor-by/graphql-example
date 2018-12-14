plugins {
  id("com.apollographql.android")
}

apollo {
  customTypeMapping.set(mapOf(
      "LocalDate" to "java.time.LocalDate",
      "LocalDateTime" to "java.time.LocalDateTime"
  ))
  useSemanticNaming.set(true)
  generateModelBuilder.set(true)
  useJavaBeansSemanticNaming.set(true)
  outputPackageName.set("com.idfinance.graphql.api")
}


dependencies {
  compile(project(":component:mm-graphql-client"))
  
  compile(deps.kodein.core)
  compile(deps.logger.slf4j)
  compile(deps.logger.logback.core)
  compile(deps.logger.logback.classic)
}