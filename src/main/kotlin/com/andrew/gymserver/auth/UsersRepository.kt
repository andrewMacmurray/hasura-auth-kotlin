package com.andrew.gymserver.auth

import CreateUserMutation
import FindUserQuery
import com.andrew.gymserver.auth.UsersError.CreateUserError
import com.andrew.gymserver.graphql.GymRepository
import com.andrew.gymserver.utils.Result
import com.andrew.gymserver.utils.nullableToResult
import com.andrew.gymserver.utils.pipe


// UsersRepository

interface UsersRepository {
    suspend fun create(createUserDetails: CreateUserDetails): Result<UserDetails, UsersError>
}

// GraphQL Users

object GraphQLUsers : UsersRepository {
    override suspend fun create(createUserDetails: CreateUserDetails): Result<UserDetails, UsersError> {
        return createAUser(createUserDetails).nullableToResult(CreateUserError)
    }
}

suspend fun createAUser(u: CreateUserDetails): UserDetails? {
    val mutation = CreateUserMutation(
        email = u.email,
        firstName = u.firstName,
        secondName = u.secondName,
        username = u.username,
        passwordHash = u.passwordHash
    )
    return GymRepository
        .mutate(mutation)
        .data()?.insert_users?.returning?.get(0)
        ?.pipe { UserDetails(it.id, it.username, it.email) }
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

