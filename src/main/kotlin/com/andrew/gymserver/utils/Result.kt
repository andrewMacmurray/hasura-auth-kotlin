package com.andrew.gymserver.utils

sealed class Result<Ok, Err> {
    data class Ok<Ok, Err>(val value: Ok) : Result<Ok, Err>()
    data class Error<O, Err>(val error: Err) : Result<O, Err>()
}

fun <OkA, Err, OkB> Result<OkA, Err>.andThen(f: (OkA) -> Result<OkB, Err>): Result<OkB, Err> =
    when (this) {
        is Result.Ok -> f(this.value)
        is Result.Error -> Result.Error(
            this.error
        )
    }

fun <OkA, Err, OkB> Result<OkA, Err>.map(f: (OkA) -> OkB): Result<OkB, Err> {
    return when (this) {
        is Result.Ok -> Result.Ok(
            f(this.value)
        )
        is Result.Error -> Result.Error(
            this.error
        )
    }
}

fun <Ok, ErrA, ErrB> Result<Ok, ErrA>.mapError(f: (ErrA) -> ErrB): Result<Ok, ErrB> {
    return when (this) {
        is Result.Ok -> Result.Ok(this.value)
        is Result.Error -> Result.Error(f(this.error))
    }
}

// Extensions

fun <Ok, Err> Ok?.nullableToResult(err: Err): Result<Ok, Err> {
    return if (this == null) {
        Result.Error(err)
    } else {
        Result.Ok(this)
    }
}

