package com.andrew.gymserver.auth

import FindUserQuery
import arrow.Kind
import arrow.fx.ForIO
import arrow.fx.extensions.io.unsafeRun.runBlocking
import arrow.unsafe
import com.andrew.gymserver.utils.Result
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Component
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class UserController(@Autowired val services: Services) {

    @GetMapping("/do-something")
    suspend fun doSomething(): FindUserQuery.User? {
        return getAUser()
    }

    @PostMapping("/create-user")
    fun createUser(@RequestBody signUpRequest: SignUpRequest): ResponseEntity<Token> =
        signUpUser(signUpRequest).toResponse()

    private fun signUpUser(signUpRequest: SignUpRequest) =
        Workflow.execute { services.users().signUp(signUpRequest) }
}

object Workflow {
    fun <T> execute(workflow: () -> Kind<ForIO, T>): T =
        unsafe { runBlocking { workflow() } }
}

private fun <Ok, Err> Result<Ok, Err>.toResponse(): ResponseEntity<Ok> {
    return when (this) {
        is Result.Ok -> ResponseEntity(this.value, HttpStatus.OK)
        is Result.Error -> ResponseEntity.badRequest().build()
    }
}

@Component
class Services(
    @Autowired private val passwordService: BCryptService,
    @Autowired private val tokenService: HasuraJWTService,
    @Autowired private val usersRepository: GraphQLUsers
) {
    fun users() = UserService(
        passwordService,
        tokenService,
        usersRepository
    )
}
