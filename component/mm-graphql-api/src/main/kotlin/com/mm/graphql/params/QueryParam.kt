package com.mm.graphql.params

data class QueryParam(
    val query: String,
    val variables: Map<String, Any?>?,
    val operationName: String?
)