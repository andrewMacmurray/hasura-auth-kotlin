package com.andrew.authserver.auth

import arrow.core.Either
import com.andrew.authserver.IOWorkflow
import com.andrew.authserver.ResponseError
import com.andrew.authserver.auth.WorkflowError.PasswordError
import com.andrew.authserver.auth.WorkflowError.TokenError
import com.andrew.authserver.auth.WorkflowError.UsersError
import com.andrew.authserver.auth.service.PasswordService
import com.andrew.authserver.auth.service.PasswordServiceError
import com.andrew.authserver.auth.service.Token
import com.andrew.authserver.auth.service.TokenService
import com.andrew.authserver.auth.service.TokenServiceError
import com.andrew.authserver.auth.service.UserDetails
import com.andrew.authserver.auth.service.UserService
import com.andrew.authserver.auth.service.UserToCreate
import com.andrew.authserver.auth.service.UsersServiceError
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
        passwordService.hash(password).mapLeft(::PasswordError)

    private fun generateToken(userDetails: UserDetails): Either<WorkflowError, Token> =
        tokenService.generate(userDetails).mapLeft(::TokenError)

    private fun createUser(userToCreate: UserToCreate): IOEither<WorkflowError, UserDetails> =
        userService.create(userToCreate).mapLeft(::UsersError)

    private fun findUser(userName: String): IOEither<WorkflowError, UserDetails> =
        userService.find(userName).mapLeft(::UsersError)

    private fun checkPassword(login: LoginRequest, details: UserDetails): Either<WorkflowError, UserDetails> =
        passwordService
            .verify(login.password, details.passwordHash)
            .mapLeft(::PasswordError)
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

// Errors

sealed class WorkflowError : ResponseError {
    data class PasswordError(val error: PasswordServiceError) : WorkflowError()
    data class UsersError(val error: UsersServiceError) : WorkflowError()
    data class TokenError(val error: TokenServiceError) : WorkflowError()

    override fun message() = errorMessage(this)
}

private fun errorMessage(e: WorkflowError): String = when (e) {
    is PasswordError -> passwordErrorMessage(e.error)
    is UsersError -> usersErrorMessage(e.error)
    is TokenError -> tokenErrorMessage(e.error)
}

private fun tokenErrorMessage(e: TokenServiceError): String = when (e) {
    TokenServiceError.CreationError -> "Error creating token"
    TokenServiceError.DecodeError -> "Error decoding token"
}

private fun passwordErrorMessage(e: PasswordServiceError): String = when (e) {
    PasswordServiceError.InvalidPassword -> invalidLogin
    PasswordServiceError.PasswordCreationError -> "Error creating password"
}

private fun usersErrorMessage(e: UsersServiceError): String = when (e) {
    UsersServiceError.DuplicateUsersError -> "User already exists"
    UsersServiceError.NetworkError -> "Error connecting to users repository"
    UsersServiceError.UserNotFoundError -> invalidLogin
}

private const val invalidLogin = "Invalid Username / Password"
