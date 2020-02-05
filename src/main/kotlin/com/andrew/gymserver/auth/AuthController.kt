package com.andrew.gymserver.auth

import com.andrew.gymserver.auth.service.AuthService
import com.andrew.gymserver.auth.service.Token
import com.andrew.gymserver.toResponse
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class UserController(@Autowired val authService: AuthService) {

    @PostMapping("/sign-up")
    suspend fun signUp(@RequestBody signUpRequest: SignUpRequest): Token =
        authService
            .signUp(signUpRequest)
            .toResponse()

    @PostMapping("/login")
    suspend fun login(@RequestBody loginRequest: LoginRequest): Token =
        authService
            .login(loginRequest)
            .toResponse()
}
