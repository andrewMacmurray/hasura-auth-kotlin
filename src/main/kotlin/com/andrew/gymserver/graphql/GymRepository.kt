package com.andrew.gymserver.graphql

import CreateUserMutation
import FindUserQuery
import com.apollographql.apollo.api.Response

object GymRepository {
    private val client = Client.create()

    suspend fun query(findUserQuery: FindUserQuery): Response<FindUserQuery.Data> {
        return client.query(findUserQuery).execute()
    }

    suspend fun mutate(createUserMutation: CreateUserMutation): Response<CreateUserMutation.Data> {
        return client.mutate(createUserMutation).execute()
    }
}
