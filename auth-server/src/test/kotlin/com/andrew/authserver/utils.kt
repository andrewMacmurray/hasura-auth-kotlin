package com.andrew.authserver

import arrow.core.Either
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertTrue

fun <Left, Right> assertOnOkValue(
    either: Either<Left, Right>,
    assertWhenOk: (Right) -> Unit
) {
    when (either) {
        is Either.Right -> assertWhenOk(either.b)
        is Either.Left -> Assertions.fail("${either.a} is not Ok")
    }
}

fun <Left, Right> assertIsOk(either: Either<Left, Right>) {
    when (either) {
        is Either.Right -> pass()
        is Either.Left -> Assertions.fail("${either.a} is not Ok")
    }
}

fun <Left, Right> assertIsError(either: Either<Left, Right>) {
    when (either) {
        is Either.Right -> Assertions.fail("${either.b} should be an Error")
        is Either.Left -> pass()
    }
}

private fun pass() {
    assertTrue(true)
}