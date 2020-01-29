package com.andrew.gymserver

import com.andrew.gymserver.utils.Result
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertTrue

fun <Ok, Err> assertOnOkValue(
    result: Result<Ok, Err>,
    assertWhenOk: (Ok) -> Unit
) {
    when (result) {
        is Result.Ok -> assertWhenOk(result.value)
        is Result.Error -> Assertions.fail("${result.error} is not Ok")
    }
}

fun <Ok, Err> assertIsOk(result: Result<Ok, Err>) {
    when (result) {
        is Result.Ok -> pass()
        is Result.Error -> Assertions.fail("${result.error} is not Ok")
    }
}

fun <Ok, Err> assertIsError(result: Result<Ok, Err>) {
    when (result) {
        is Result.Ok -> Assertions.fail("${result.value} should be an Error")
        is Result.Error -> pass()
    }
}

private fun pass() {
    assertTrue(true)
}