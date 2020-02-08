package com.andrew.authserver.auth

import arrow.syntax.function.pipe
import com.andrew.authserver.assertIsError
import com.andrew.authserver.assertIsOk
import com.andrew.authserver.auth.service.BCryptService
import org.junit.jupiter.api.Test

class PasswordServiceTest {

    private val passwordService = BCryptService

    @Test
    fun `hashes and verifies a password`() {
        val password = "abCDE12345678!"
        passwordService
            .hash(password)
            .map { passwordService.verify(password, it) }
            .pipe(::assertIsOk)
    }

    @Test
    fun `password must contain at least 8 characters`() {
        val shortPassword = "abc123"
        passwordService
            .hash(shortPassword)
            .pipe(::assertIsError)
    }

    @Test
    fun `password should contain a mixture of numbers and letters`() {
        val onlyLetters = "acbDefghijK"
        val onlyNumbers = "12345678910"

        passwordService
            .hash(onlyLetters)
            .pipe(::assertIsError)

        passwordService
            .hash(onlyNumbers)
            .pipe(::assertIsError)
    }
}