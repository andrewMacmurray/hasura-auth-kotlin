package com.andrew.gymserver.graphql

import com.andrew.gymserver.utils.pipe
import com.apollographql.apollo.ApolloCall
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.Response
import com.apollographql.apollo.exception.ApolloException
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine
import okhttp3.Response as OkhttpResponse

object Client {
    fun create(): ApolloClient =
        ApolloClient
            .builder()
            .serverUrl("http://localhost:8080/v1/graphql")
            .okHttpClient(okHttpClient)
            .build()

    private val okHttpClient =
        OkHttpClient
            .Builder()
            .addInterceptor(this::addHeaders)
            .build()

    private fun addHeaders(chain: Interceptor.Chain): OkhttpResponse =
        chain.request()
            .pipe { it.newBuilder().method(it.method(), it.body()) }
            .pipe { it.header("x-hasura-admin-secret", "ilovebread") }
            .pipe { chain.proceed(it.build()) }
//        val original = chain.request()
//        val builder = original.newBuilder().method(original.method(), original.body())
//        builder.header("x-hasura-admin-secret", "ilovebread")
//        return chain.proceed(builder.build())
}

suspend fun <T> ApolloCall<T>.execute() =
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
