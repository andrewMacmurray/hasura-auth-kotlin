package com.andrew.authserver.auth

import arrow.core.Either
import com.andrew.authserver.IOWorkflow
import com.andrew.authserver.ResponseError
import com.andrew.authserver.auth.WorkflowError.HashPasswordError
import com.andrew.authserver.auth.WorkflowError.TokenGenerationError
import com.andrew.authserver.auth.WorkflowError.UsersErrorMain
import com.andrew.authserver.auth.service.PasswordError
import com.andrew.authserver.auth.service.PasswordService
import com.andrew.authserver.auth.service.Token
import com.andrew.authserver.auth.service.TokenError
import com.andrew.authserver.auth.service.TokenService
import com.andrew.authserver.auth.service.UserDetails
import com.andrew.authserver.auth.service.UserService
import com.andrew.authserver.auth.service.UserToCreate
import com.andrew.authserver.auth.service.UsersError
import com.andrew.authserver.utils.IOEither
import com.andrew.authserver.utils.flatMap
import com.andrew.authserver.utils.liftEither
import com.andrew.authserver.utils.mapLeft
import com.andrew.authserver.utils.run
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class AuthService(
    @Autowired private val passwordService: PasswordService,
    @Autowired private val tokenService: TokenService,
    @Autowired private val userService: UserService
) {
    fun signUp(signUpRequest: SignUpRequest): Either<WorkflowError, Token> =
        IOWorkflow.execute { signUpWorkflow(signUpRequest).run() }

    fun login(loginRequest: LoginRequest): Either<WorkflowError, Token> =
        IOWorkflow.execute { loginWorkflow(loginRequest).run() }

    private fun signUpWorkflow(signUpRequest: SignUpRequest): IOEither<WorkflowError, Token> =
        hashPassword(signUpRequest.password)
            .map { hash -> userToCreate(signUpRequest, hash) }
            .liftEither()
            .flatMap(this::createUser)
            .flatMap { generateToken(it).liftEither() }

    private fun loginWorkflow(loginRequest: LoginRequest): IOEither<WorkflowError, Token> =
        findUser(loginRequest.username)
            .flatMap { checkPassword(loginRequest, it).liftEither() }
            .flatMap { generateToken(it).liftEither() }

    private fun hashPassword(password: String): Either<WorkflowError, String> =
        passwordService.hash(password).mapLeft(::HashPasswordError)

    private fun generateToken(userDetails: UserDetails): Either<WorkflowError, Token> =
        tokenService.generate(userDetails).mapLeft(::TokenGenerationError)

    private fun createUser(userToCreate: UserToCreate): IOEither<WorkflowError, UserDetails> =
        userService.create(userToCreate).mapLeft(::UsersErrorMain)

    private fun findUser(userName: String): IOEither<WorkflowError, UserDetails> =
        userService.find(userName).mapLeft(::UsersErrorMain)

    private fun checkPassword(login: LoginRequest, details: UserDetails): Either<WorkflowError, UserDetails> =
        passwordService
            .verify(login.password, details.passwordHash)
            .mapLeft(::HashPasswordError)
            .map { details }
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

private fun <Left, Right> Either<Left, Right>.liftEither(): IOEither<Left, Right> =
    liftEither(this)

// Errors

sealed class WorkflowError : ResponseError {
    data class HashPasswordError(val e: PasswordError) : WorkflowError()
    data class UsersErrorMain(val e: UsersError) : WorkflowError()
    data class TokenGenerationError(val e: TokenError) : WorkflowError()

    override fun message() = errorMessage(this)
}

private fun errorMessage(err: WorkflowError): String = when (err) {
    is HashPasswordError -> passwordErrorMessage(err.e)
    is UsersErrorMain -> usersErrorMessage(err.e)
    is TokenGenerationError -> tokenErrorMessage(err.e)
}

private fun tokenErrorMessage(e: TokenError): String = when (e) {
    TokenError.CreationError -> "Error creating token"
    TokenError.DecodeError -> "Error decoding token"
}

private fun passwordErrorMessage(e: PasswordError): String = when (e) {
    PasswordError.InvalidPassword -> invalidLogin
    PasswordError.PasswordCreationError -> "Error creating password"
}

private fun usersErrorMessage(e: UsersError): String = when (e) {
    UsersError.DuplicateUsersError -> "User already exists"
    UsersError.NetworkError -> "Error connecting to users repository"
    UsersError.UserNotFoundError -> invalidLogin
}

private const val invalidLogin = "Invalid Username / Password"