package com.andrew.gymserver.auth

import com.andrew.gymserver.auth.UsersError.CreateUserError
import com.andrew.gymserver.utils.Result


// UsersRepository

interface UsersRepository {
    fun create(createUserDetails: CreateUserDetails): Result<UserDetails, UsersError>
}

// GraphQL Users

object GraphQLUsers : UsersRepository {
    override fun create(createUserDetails: CreateUserDetails): Result<UserDetails, UsersError> {
        return Result.Error(CreateUserError)
    }
}

// Errors

sealed class UsersError {
    object CreateUserError: UsersError()
}

