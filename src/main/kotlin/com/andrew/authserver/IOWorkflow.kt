package com.andrew.authserver

import arrow.Kind
import arrow.fx.ForIO
import arrow.fx.extensions.io.unsafeRun.runBlocking
import arrow.unsafe

object IOWorkflow {
    fun <T> execute(workflow: () -> Kind<ForIO, T>): T =
        unsafe { runBlocking { workflow() } }
}

