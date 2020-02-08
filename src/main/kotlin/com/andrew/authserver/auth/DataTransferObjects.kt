package com.andrew.authserver.auth

data class SignUpRequest(
    val username: String,
    val firstName: String,
    val secondName: String,
    val email: String,
    val password: String
)

data class LoginRequest(
    val username: String,
    val password: String
)
