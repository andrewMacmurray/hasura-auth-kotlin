package com.andrew.gymserver.auth

import com.andrew.gymserver.auth.TokenError.CreationError
import com.andrew.gymserver.auth.TokenError.DecodeError
import com.andrew.gymserver.utils.Result
import com.andrew.gymserver.utils.Result.Ok
import com.andrew.gymserver.utils.pipe
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.exceptions.JWTCreationException
import com.auth0.jwt.exceptions.JWTDecodeException
import com.auth0.jwt.interfaces.DecodedJWT
import org.springframework.stereotype.Component


// Token

data class Token(val token: String)

// Token Generator

interface TokenService {
    fun generate(userDetails: UserDetails): Result<Token, TokenError>
    fun decode(token: Token): Result<DecodedJWT, TokenError>
}

// JWT

@Component
object HasuraJWTService : TokenService {
    private val algorithm = Algorithm.HMAC256("ilovebread")
    private const val hasuraClaimName = "https://hasura.io/jwt/claims"

    override fun generate(userDetails: UserDetails): Result<Token, TokenError> = try {
        createToken(userDetails)
    } catch (e: JWTCreationException) {
        Result.Error(CreationError)
    }

    private fun createToken(userDetails: UserDetails): Result<Token, TokenError> =
        JWT.create()
            .withClaim("username", userDetails.username)
            .withClaim("email", userDetails.email)
            .withClaim(hasuraClaimName, toHasuraClaims(userDetails))
            .sign(algorithm)
            .pipe(::Token)
            .pipe(::Ok)

    private fun toHasuraClaims(userDetails: UserDetails): String {
        return """
            {
              "x-hasura-allowed-roles": ["user"],
              "x-hasura-default-role": "user",
              "x-hasura-user-id": ${userDetails.id}
            }
        """.trimIndent()
    }

    override fun decode(token: Token): Result<DecodedJWT, TokenError> = try {
        JWT.decode(token.token).pipe(::Ok)
    } catch (e: JWTDecodeException) {
        Result.Error(DecodeError)
    }
}

// Errors

sealed class TokenError {
    object CreationError : TokenError()
    object DecodeError : TokenError()
}
