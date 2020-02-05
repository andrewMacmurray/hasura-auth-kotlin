package com.andrew.gymserver.auth

import arrow.core.flatMap
import com.andrew.gymserver.assertOnOkValue
import com.andrew.gymserver.auth.service.HasuraJWTService
import com.andrew.gymserver.auth.service.UserDetails
import com.auth0.jwt.interfaces.DecodedJWT
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class TokenServiceTest {

    private val tokenService = HasuraJWTService

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
            val hasuraClaimName = "https://hasura.io/jwt/claims"
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
            email = "a@b.com"
        )
}