package com.andrew.gymserver.auth

data class SignUpRequest(
    val username: String,
    val firstName: String,
    val secondName: String,
    val email: String,
    val password: String
)
