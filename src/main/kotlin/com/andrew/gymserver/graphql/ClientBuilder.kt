package com.andrew.gymserver.graphql

import arrow.syntax.function.pipe
import com.apollographql.apollo.ApolloClient
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class ClientBuilder(
    @Value("\${hasura.server_url}") val serverUrl: String,
    @Value("\${hasura.admin_secret}") val adminSecret: String
) {
    fun build(): ApolloClient =
        ApolloClient
            .builder()
            .serverUrl(serverUrl)
            .okHttpClient(buildOkHttpClient())
            .build()

    private fun buildOkHttpClient() =
        OkHttpClient
            .Builder()
            .addInterceptor(this::addHeaders)
            .build()

    private fun addHeaders(chain: Interceptor.Chain): Response =
        chain.request()
            .pipe { it.newBuilder().method(it.method(), it.body()) }
            .pipe { it.header("x-hasura-admin-secret", adminSecret) }
            .pipe { chain.proceed(it.build()) }
}

