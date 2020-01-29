package com.andrew.gymserver.graphql

import com.andrew.gymserver.utils.pipe
import com.apollographql.apollo.ApolloClient
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response

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

    private fun addHeaders(chain: Interceptor.Chain): Response =
        chain.request()
            .pipe { it.newBuilder().method(it.method(), it.body()) }
            .pipe { it.header("x-hasura-admin-secret", "ilovebread") }
            .pipe { chain.proceed(it.build()) }
}

