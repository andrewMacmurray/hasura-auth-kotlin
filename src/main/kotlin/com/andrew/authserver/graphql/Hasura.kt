package com.andrew.authserver.graphql

import com.apollographql.apollo.ApolloCall
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.Mutation
import com.apollographql.apollo.api.Operation
import com.apollographql.apollo.api.Query
import com.apollographql.apollo.api.Response
import com.apollographql.apollo.exception.ApolloException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

@Component
class Hasura(@Autowired final val clientBuilder: ClientBuilder) {

    private val client: ApolloClient =
        clientBuilder.build()

    suspend fun <T : Operation.Data> query(queryData: Query<T, T, Operation.Variables>): T? {
        return client.query(queryData).execute().data()
    }

    suspend fun <T : Operation.Data> mutate(mutationData: Mutation<T, T, Operation.Variables>): T? {
        return client.mutate(mutationData).execute().data()
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
