package com.andrew.gymserver.auth

import arrow.fx.IO
import com.andrew.gymserver.auth.WorkflowError.HashPasswordError
import com.andrew.gymserver.auth.WorkflowError.TokenGenerationError
import com.andrew.gymserver.auth.WorkflowError.UserCreationError
import com.andrew.gymserver.utils.Result
import com.andrew.gymserver.utils.map
import com.andrew.gymserver.utils.mapError
import com.andrew.gymserver.utils.pipe

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
    fun signUp(signUpRequest: SignUpRequest): IO<Result<Token, WorkflowError>> {
        return hashPassword(signUpRequest)
            .map { hash -> createUserDetails(signUpRequest, hash) }
            .pipe { it.liftIO() }
            .andThen { IO { createUser(it) } }
            .andThen { generateToken(it).liftIO()  }
    }

    private fun hashPassword(signUpRequest: SignUpRequest): Result<String, WorkflowError> =
        passwordService.hash(signUpRequest.password).mapError { HashPasswordError }

    private fun generateToken(userDetails: UserDetails): Result<Token, WorkflowError> =
        tokenService.generate(userDetails).mapError { TokenGenerationError }

    private suspend fun createUser(createUserDetails: CreateUserDetails): Result<UserDetails, WorkflowError> =
        usersRepository.create(createUserDetails).mapError { UserCreationError }
}

private fun <T> T.liftIO(): IO<T> = IO { this }

private fun <Ok, Ok2, Err> IO<Result<Ok, Err>>.andThen(function: (Ok) -> IO<Result<Ok2, Err>>): IO<Result<Ok2, Err>> {
    return this.flatMap {
        when (it) {
            is Result.Ok -> function(it.value)
            is Result.Error -> IO { Result.Error<Ok2, Err>(it.error) }
        }
    }
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