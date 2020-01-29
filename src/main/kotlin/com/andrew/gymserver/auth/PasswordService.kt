package com.andrew.gymserver.auth

import com.andrew.gymserver.auth.PasswordError.InvalidPassword
import com.andrew.gymserver.auth.PasswordError.PasswordCreationError
import com.andrew.gymserver.utils.Result
import com.andrew.gymserver.utils.nullableToResult
import org.mindrot.jbcrypt.BCrypt


// PasswordService

interface PasswordService {
    fun hash(password: String): Result<String, PasswordError>
    fun verify(password: String, hash: String): Result<Unit, PasswordError>
}

// BCrypt Service

object BCryptService : PasswordService {
    override fun hash(password: String): Result<String, PasswordError> =
        if (password.meetsCriteria())
            hashWithSalt(password).nullableToResult(PasswordCreationError)
        else
            Result.Error(PasswordCreationError)

    override fun verify(password: String, hash: String): Result<Unit, PasswordError> =
        if (password.matchesHash(hash))
            Result.Ok(Unit)
        else
            Result.Error(InvalidPassword)

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

sealed class PasswordError {
    object InvalidPassword : PasswordError()
    object PasswordCreationError : PasswordError()
}
