package com.andrew.authserver

import arrow.core.Either
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice
import java.util.Date

@RestControllerAdvice
class ControllerExceptionHandler {

    @ExceptionHandler(WorkflowException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleWorkflowError(ex: WorkflowException) = ErrorMessage(
        message = ex.message ?: "something went wrong",
        date = Date()
    )
}

data class ErrorMessage(val message: String, val date: Date)

class WorkflowException(message: String) : Exception(message)


// Either to Response

interface ResponseError {
    fun message(): String
}

fun <Right> Either<ResponseError, Right>.toResponse() =
    when (this) {
        is Either.Left -> throw WorkflowException(this.a.message())
        is Either.Right -> this.b
    }
