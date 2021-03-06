package com.andrew.authserver.auth

import arrow.core.flatMap
import com.andrew.authserver.assertOnOkValue
import com.andrew.authserver.auth.service.HasuraJWTService
import com.andrew.authserver.auth.service.UserDetails
import com.auth0.jwt.interfaces.DecodedJWT
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class TokenServiceTest {

    private val hasuraClaimName = "https://hasura.io/jwt/claims"
    private val jwtSecret = "very-secret-secret"
    private val tokenService = buildTokenService(jwtSecret, hasuraClaimName)

    @Test
    fun `generates a JWT with username and email claims`() {
        val details = userDetails()
        val token = tokenService
            .generate(details)
            .flatMap(tokenService::decode)

        assertOnOkValue(token) {
            assertEquals(details.username, claimValueFor("username", it))
            assertEquals(details.email, claimValueFor("email", it))
        }
    }

    @Test
    fun `generates a JWT with hasura claims`() {
        val details = userDetails()
        val token = tokenService
            .generate(details)
            .flatMap(tokenService::decode)

        assertOnOkValue(token) {
            val hasuraClaim = claimValueFor(hasuraClaimName, it)

            assertTrue(hasuraClaim.contains("\"x-hasura-user-id\": ${details.id}"))
            assertTrue(hasuraClaim.contains("\"x-hasura-default-role\": \"user\""))
            assertTrue(hasuraClaim.contains("\"x-hasura-allowed-roles\": [\"user\"]"))
        }
    }

    private fun claimValueFor(claimName: String, jwt: DecodedJWT) =
        jwt.getClaim(claimName).asString()

    private fun userDetails() =
        UserDetails(
            id = 1,
            username = "andrew",
            email = "a@b.com",
            passwordHash = "abc123£ashdajskd"
        )

    private fun buildTokenService(secret: String, claimName: String) =
        HasuraJWTService(
            jwtSecret = secret,
            claimNamespace = claimName
        )
}