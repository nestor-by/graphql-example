rootProject.name = "graphql-example"

include(
    ":graphql-test-server",
    ":graphql-test-client",
    ":component:mm-graphql-api",
    ":component:mm-graphql-client",
    ":component:mm-graphql-jooq",
    ":component:mm-graphql-mongodb"
)