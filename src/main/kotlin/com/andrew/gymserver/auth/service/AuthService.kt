package com.andrew.gymserver.auth.service

import arrow.core.Either
import arrow.fx.fix
import arrow.syntax.function.pipe
import com.andrew.gymserver.IOWorkflow
import com.andrew.gymserver.ResponseError
import com.andrew.gymserver.auth.SignUpRequest
import com.andrew.gymserver.auth.service.WorkflowError.HashPasswordError
import com.andrew.gymserver.auth.service.WorkflowError.TokenGenerationError
import com.andrew.gymserver.auth.service.WorkflowError.UserCreationError
import com.andrew.gymserver.utils.IOEither
import com.andrew.gymserver.utils.flatMap
import com.andrew.gymserver.utils.liftEither
import com.andrew.gymserver.utils.mapLeft

data class UserToCreate(
    val username: String,
    val email: String,
    val firstName: String,
    val secondName: String,
    val passwordHash: String
)

data class UserDetails(
    val id: Int,
    val username: String,
    val email: String
)

class AuthService(
    private val passwordService: PasswordService,
    private val tokenService: TokenService,
    private val usersRepository: UsersRepository
) {
    fun signUp(signUpRequest: SignUpRequest): Either<WorkflowError, Token> =
        IOWorkflow.execute { signUpWorkflow(signUpRequest).value().fix() }

    private fun signUpWorkflow(signUpRequest: SignUpRequest): IOEither<WorkflowError, Token> {
        return hashPassword(signUpRequest.password)
            .map { hash -> userToCreate(signUpRequest, hash) }
            .pipe(::liftEither)
            .flatMap(this::createUser)
            .flatMap { generateToken(it).pipe(::liftEither) }
    }

    private fun hashPassword(password: String): Either<WorkflowError, String> =
        passwordService.hash(password).mapLeft(::HashPasswordError)

    private fun generateToken(userDetails: UserDetails): Either<WorkflowError, Token> =
        tokenService.generate(userDetails).mapLeft(::TokenGenerationError)

    private fun createUser(userToCreate: UserToCreate): IOEither<WorkflowError, UserDetails> =
        usersRepository.create(userToCreate).mapLeft(::UserCreationError)
}

// Helpers

private fun userToCreate(request: SignUpRequest, passwordHash: String) =
    UserToCreate(
        request.username,
        request.email,
        request.firstName,
        request.secondName,
        passwordHash
    )

// Errors

sealed class WorkflowError : ResponseError {
    data class HashPasswordError(val err: PasswordError) : WorkflowError()
    data class UserCreationError(val err: UsersError) : WorkflowError()
    data class TokenGenerationError(val err: TokenError) : WorkflowError()

    override fun message(): String = when (this) {
        is HashPasswordError -> this.err.message()
        is UserCreationError -> this.err.message()
        is TokenGenerationError -> this.err.message()
    }
}