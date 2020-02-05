package com.andrew.gymserver.auth

import com.andrew.gymserver.auth.service.AuthService
import com.andrew.gymserver.auth.service.BCryptService
import com.andrew.gymserver.auth.service.GraphQLUsers
import com.andrew.gymserver.auth.service.HasuraJWTService
import com.andrew.gymserver.auth.service.Token
import com.andrew.gymserver.toResponse
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class UserController(@Autowired val service: Service) {

    @PostMapping("/create-user")
    suspend fun createUser(@RequestBody signUpRequest: SignUpRequest): Token =
        service.auth()
            .signUp(signUpRequest)
            .toResponse()
}

@Component
class Service(
    @Autowired private val passwordService: BCryptService,
    @Autowired private val tokenService: HasuraJWTService,
    @Autowired private val usersRepository: GraphQLUsers
) {
    fun auth() = AuthService(
        passwordService,
        tokenService,
        usersRepository
    )
}
