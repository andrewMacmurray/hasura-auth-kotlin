package com.andrew.gymserver.auth.service

import CreateUserMutation
import arrow.core.Either
import arrow.core.Either.Companion.left
import arrow.fx.IO
import arrow.mtl.EitherT
import arrow.syntax.function.pipe
import com.andrew.gymserver.auth.service.UsersError.DuplicateUsersError
import com.andrew.gymserver.auth.service.UsersError.NetworkError
import com.andrew.gymserver.graphql.GymRepository
import com.andrew.gymserver.utils.IOEither
import com.andrew.gymserver.utils.nullableToEither
import com.apollographql.apollo.exception.ApolloNetworkException
import org.springframework.stereotype.Component


// UsersRepository

interface UsersRepository {
    fun create(userToCreate: UserToCreate): IOEither<UsersError, UserDetails>
}

// GraphQL Users

@Component
object GraphQLUsers : UsersRepository {
    override fun create(userToCreate: UserToCreate): IOEither<UsersError, UserDetails> =
        IO { createAUser(userToCreate) }.pipe { EitherT.invoke(it) }
}

suspend fun createAUser(user: UserToCreate): Either<UsersError, UserDetails> {
    return try {
        createUserMutation(user)
            .pipe { GymRepository.mutate(it).data() }
            ?.insert_users
            ?.returning
            ?.get(0)
            ?.pipe(::userDetails)
            .nullableToEither(DuplicateUsersError)
    } catch (e: ApolloNetworkException) {
        left(NetworkError)
    }
}

private fun userDetails(user: CreateUserMutation.Returning) =
    UserDetails(
        id = user.id,
        username = user.username,
        email = user.email
    )

private fun createUserMutation(u: UserToCreate) = CreateUserMutation(
    email = u.email,
    firstName = u.firstName,
    secondName = u.secondName,
    username = u.username,
    passwordHash = u.passwordHash
)

// Errors

sealed class UsersError {
    object DuplicateUsersError : UsersError()
    object NetworkError : UsersError()
}

fun UsersError.message(): String = when (this) {
    DuplicateUsersError -> "User already exists"
    NetworkError -> "Error connecting to users repository"
}


