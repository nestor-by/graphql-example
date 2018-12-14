dependencies {
  compile(project(":component:mm-graphql-api"))

  compile(deps.kodein.core)
  compile(deps.mongodb.async)
}