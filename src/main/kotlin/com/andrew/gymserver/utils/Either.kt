package com.andrew.gymserver.utils

import arrow.core.Either

fun <Left, Right> Right?.nullableToEither(left: Left): Either<Left, Right> {
    return if (this == null) {
        Either.left(left)
    } else {
        Either.right(this)
    }
}