package com.andrew.authserver.auth.service

import arrow.core.Either
import arrow.core.Either.Companion.left
import arrow.core.Either.Companion.right
import com.andrew.authserver.auth.service.PasswordServiceError.InvalidPassword
import com.andrew.authserver.auth.service.PasswordServiceError.PasswordCreationError
import com.andrew.authserver.utils.nullableToEither
import org.mindrot.jbcrypt.BCrypt
import org.springframework.stereotype.Component

// PasswordService

interface PasswordService {
    fun hash(password: String): Either<PasswordServiceError, String>
    fun verify(password: String, hash: String): Either<PasswordServiceError, Unit>
}

// BCrypt Service

@Component
object BCryptService : PasswordService {
    override fun hash(password: String): Either<PasswordServiceError, String> =
        if (password.meetsCriteria())
            hashWithSalt(password).nullableToEither(PasswordCreationError)
        else
            left(PasswordCreationError)

    override fun verify(password: String, hash: String): Either<PasswordServiceError, Unit> =
        if (password.matchesHash(hash)) right(Unit) else left(InvalidPassword)

    private fun hashWithSalt(password: String): String? =
        BCrypt.hashpw(password, BCrypt.gensalt())

    private fun String.meetsCriteria(): Boolean =
        PasswordCriteria.passes(this)

    private fun String.matchesHash(hash: String): Boolean =
        BCrypt.checkpw(this, hash)
}

private object PasswordCriteria {
    private const val hasLowerCase = "(?=.*[a-z])"
    private const val hasUpperCase = "(?=.*[A-Z])"
    private const val hasNumbers = "(?=.*[0-9])"
    private const val atLeast8Characters = "(?=.{8,})"

    fun passes(password: String): Boolean =
        password.contains("^$hasLowerCase$hasUpperCase$hasNumbers$atLeast8Characters".toRegex())
}

// Errors

sealed class PasswordServiceError {
    object InvalidPassword : PasswordServiceError()
    object PasswordCreationError : PasswordServiceError()
}
