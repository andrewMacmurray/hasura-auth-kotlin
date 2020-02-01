package com.andrew.gymserver.auth

import com.andrew.gymserver.auth.WorkflowError.HashPasswordError
import com.andrew.gymserver.auth.WorkflowError.TokenGenerationError
import com.andrew.gymserver.auth.WorkflowError.UserCreationError
import com.andrew.gymserver.utils.Result
import com.andrew.gymserver.utils.mapError

data class CreateUserDetails(
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

class UserService(
    private val passwordService: PasswordService,
    private val tokenService: TokenService,
    private val usersRepository: UsersRepository
) {
    suspend fun signUp(signUpRequest: SignUpRequest): Result<Token, WorkflowError> {
        return Result.Error(UserCreationError)
//        return hashPassword(signUpRequest)
//            .map { hash -> createUserDetails(signUpRequest, hash) }
//            .andThen { createUser(it) }
//            .andThen { generateToken(it) }
    }

    private fun hashPassword(signUpRequest: SignUpRequest): Result<String, WorkflowError> =
        passwordService.hash(signUpRequest.password).mapError { HashPasswordError }

    private fun generateToken(userDetails: UserDetails): Result<Token, WorkflowError> =
        tokenService.generate(userDetails).mapError { TokenGenerationError }

    private suspend fun createUser(createUserDetails: CreateUserDetails): Result<UserDetails, WorkflowError> =
        usersRepository.create(createUserDetails).mapError { UserCreationError }
}

// Helpers

private fun createUserDetails(request: SignUpRequest, passwordHash: String) =
    CreateUserDetails(
        request.username,
        request.email,
        request.firstName,
        request.secondName,
        passwordHash
    )

// Errors

sealed class WorkflowError {
    object HashPasswordError : WorkflowError()
    object UserCreationError : WorkflowError()
    object TokenGenerationError : WorkflowError()
}