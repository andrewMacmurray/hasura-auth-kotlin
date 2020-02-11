package com.andrew.authserver.auth.service

import CreateUserMutation
import FindUserQuery
import arrow.syntax.function.pipe
import com.andrew.authserver.auth.service.UsersServiceError.DuplicateUsersError
import com.andrew.authserver.auth.service.UsersServiceError.NetworkError
import com.andrew.authserver.auth.service.UsersServiceError.UserNotFoundError
import com.andrew.authserver.graphql.Hasura
import com.andrew.authserver.utils.IOEither
import com.andrew.authserver.utils.captureIO
import com.andrew.authserver.utils.onException
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
    fun create(userToCreate: UserToCreate): IOEither<UsersServiceError, UserDetails>
    fun find(userName: String): IOEither<UsersServiceError, UserDetails>
}

// GraphQL Users

@Component
class GraphQLUsers(@Autowired val hasura: Hasura) : UserService {

    override fun create(userToCreate: UserToCreate): IOEither<UsersServiceError, UserDetails> =
        createAUser(userToCreate).onHasuraError(DuplicateUsersError)

    override fun find(userName: String): IOEither<UsersServiceError, UserDetails> =
        findUser(userName).onHasuraError(UserNotFoundError)

    private fun findUser(userName: String): IOEither<UsersServiceError, UserDetails> = captureIO {
        findUserQuery(userName)
            .pipe { hasura.query(it) }!!
            .users[0]
            .pipe(::userDetails)
    }

    private fun createAUser(user: UserToCreate): IOEither<UsersServiceError, UserDetails> = captureIO {
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

sealed class UsersServiceError {
    object DuplicateUsersError : UsersServiceError()
    object NetworkError : UsersServiceError()
    object UserNotFoundError : UsersServiceError()
}

// Utils

private fun <T> IOEither<UsersServiceError, T>.onHasuraError(err: UsersServiceError): IOEither<UsersServiceError, T> =
    this.onException { exception ->
        when (exception) {
            is ApolloNetworkException -> NetworkError
            is IndexOutOfBoundsException -> err
            is NullPointerException -> err
            else -> throw exception
        }
    }
