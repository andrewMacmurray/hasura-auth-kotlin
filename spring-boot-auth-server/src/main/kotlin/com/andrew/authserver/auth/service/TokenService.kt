package com.andrew.authserver.auth.service

import arrow.core.Either
import arrow.core.Either.Companion.left
import arrow.core.Either.Companion.right
import arrow.syntax.function.pipe
import com.andrew.authserver.auth.service.TokenServiceError.CreationError
import com.andrew.authserver.auth.service.TokenServiceError.DecodeError
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.exceptions.JWTCreationException
import com.auth0.jwt.exceptions.JWTDecodeException
import com.auth0.jwt.interfaces.DecodedJWT
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component


// Token

data class Token(val token: String)

// Token Generator

interface TokenService {
    fun generate(userDetails: UserDetails): Either<TokenServiceError, Token>
    fun decode(token: Token): Either<TokenServiceError, DecodedJWT>
}

// JWT

@Component
class HasuraJWTService(
    @Value("\${hasura.jwt_secret}") final val jwtSecret: String,
    @Value("\${hasura.claim_namespace}") val claimNamespace: String
) : TokenService {

    private val algorithm = Algorithm.HMAC256(jwtSecret)

    override fun generate(userDetails: UserDetails): Either<TokenServiceError, Token> = try {
        createToken(userDetails)
    } catch (e: JWTCreationException) {
        left(CreationError)
    }

    private fun createToken(userDetails: UserDetails): Either<TokenServiceError, Token> =
        JWT.create()
            .withClaim("username", userDetails.username)
            .withClaim("email", userDetails.email)
            .withClaim(claimNamespace, toHasuraClaims(userDetails))
            .sign(algorithm)
            .pipe(::Token)
            .pipe(::right)

    private fun toHasuraClaims(userDetails: UserDetails): String {
        return """
            {
              "x-hasura-allowed-roles": ["user"],
              "x-hasura-default-role": "user",
              "x-hasura-user-id": ${userDetails.id}
            }
        """.trimIndent()
    }

    override fun decode(token: Token): Either<TokenServiceError, DecodedJWT> = try {
        JWT.decode(token.token).pipe(::right)
    } catch (e: JWTDecodeException) {
        left(DecodeError)
    }
}

// Errors

sealed class TokenServiceError {
    object CreationError : TokenServiceError()
    object DecodeError : TokenServiceError()
}
