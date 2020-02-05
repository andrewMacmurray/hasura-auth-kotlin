package com.andrew.gymserver.auth.service

import CreateUserMutation
import FindUserQuery
import arrow.syntax.function.pipe
import com.andrew.gymserver.auth.service.UsersError.DuplicateUsersError
import com.andrew.gymserver.auth.service.UsersError.NetworkError
import com.andrew.gymserver.auth.service.UsersError.UserNotFoundError
import com.andrew.gymserver.graphql.Hasura
import com.andrew.gymserver.utils.IOEither
import com.andrew.gymserver.utils.captureIO
import com.andrew.gymserver.utils.onException
import com.apollographql.apollo.exception.ApolloNetworkException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.lang.NullPointerException


// Data

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
    val email: String,
    val passwordHash: String
)

// Users Service

interface UserService {
    fun create(userToCreate: UserToCreate): IOEither<UsersError, UserDetails>
    fun find(userName: String): IOEither<UsersError, UserDetails>
}

// GraphQL Users

@Component
class GraphQLUsers(@Autowired val hasura: Hasura) : UserService {

    override fun create(userToCreate: UserToCreate): IOEither<UsersError, UserDetails> =
        createAUser(userToCreate).onHasuraError(DuplicateUsersError)

    override fun find(userName: String): IOEither<UsersError, UserDetails> =
        findUser(userName).onHasuraError(UserNotFoundError)

    private fun findUser(userName: String): IOEither<UsersError, UserDetails> = captureIO {
        findUserQuery(userName)
            .pipe { hasura.query(it) }!!
            .users[0]
            .pipe(::userDetails)
    }

    private fun createAUser(user: UserToCreate): IOEither<UsersError, UserDetails> = captureIO {
        createUserMutation(user)
            .pipe { hasura.mutate(it) }!!
            .insert_users!!
            .returning[0]
            .pipe(::userDetails)
    }
}

private fun userDetails(user: CreateUserMutation.Returning) = UserDetails(
    id = user.id,
    username = user.username,
    email = user.email,
    passwordHash = user.password_hash
)

private fun userDetails(user: FindUserQuery.User) = UserDetails(
    id = user.id,
    username = user.username,
    email = user.email,
    passwordHash = user.password_hash
)

private fun findUserQuery(userName: String) =
    FindUserQuery(userName)

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
    object UserNotFoundError : UsersError()
}

// Utils

private fun <T> IOEither<UsersError, T>.onHasuraError(err: UsersError): IOEither<UsersError, T> =
    this.onException { exception ->
        when (exception) {
            is ApolloNetworkException -> NetworkError
            is IndexOutOfBoundsException -> err
            is NullPointerException -> err
            else -> throw exception
        }
    }
