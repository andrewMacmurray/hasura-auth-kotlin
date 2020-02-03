package com.andrew.gymserver.graphql

import CreateUserMutation
import FindUserQuery
import com.apollographql.apollo.ApolloCall
import com.apollographql.apollo.api.Response
import com.apollographql.apollo.exception.ApolloException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

object GymRepository {
    private val client = Client.create()

    suspend fun query(findUserQuery: FindUserQuery): Response<FindUserQuery.Data> {
        return client.query(findUserQuery).execute()
    }

    suspend fun mutate(createUserMutation: CreateUserMutation): Response<CreateUserMutation.Data> {
        return client.mutate(createUserMutation).execute()
    }
}

// Utils

private suspend fun <T> ApolloCall<T>.execute() =
    suspendCoroutine<Response<T>> { cont ->
        enqueue(object : ApolloCall.Callback<T>() {
            override fun onFailure(e: ApolloException) {
                cont.resumeWithException(e)
            }

            override fun onResponse(response: Response<T>) {
                cont.resume(response)
            }
        })
    }
