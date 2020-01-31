package com.andrew.gymserver.auth

import FindUserQuery
import com.andrew.gymserver.auth.UsersError.CreateUserError
import com.andrew.gymserver.graphql.GymRepository
import com.andrew.gymserver.utils.Result


// UsersRepository

interface UsersRepository {
    suspend fun create(createUserDetails: CreateUserDetails): Result<UserDetails, UsersError>
}

// GraphQL Users

object GraphQLUsers : UsersRepository {
    override suspend fun create(createUserDetails: CreateUserDetails): Result<UserDetails, UsersError> {
        return Result.Error(CreateUserError)
    }
}

suspend fun getAUser(): FindUserQuery.User? {
    val findUserQuery = FindUserQuery("amacmurray")
    return GymRepository
        .query(findUserQuery)
        .data()?.users?.get(0)
}


// Errors

sealed class UsersError {
    object CreateUserError : UsersError()
}

