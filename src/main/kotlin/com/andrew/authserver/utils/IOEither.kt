package com.andrew.authserver.utils

import arrow.core.Either
import arrow.core.Either.Companion.right
import arrow.fx.ForIO
import arrow.fx.IO
import arrow.fx.extensions.io.applicative.applicative
import arrow.fx.extensions.io.applicativeError.applicativeError
import arrow.fx.extensions.io.functor.functor
import arrow.fx.extensions.io.monad.monad
import arrow.fx.fix
import arrow.mtl.EitherT
import arrow.mtl.extensions.eithert.applicativeError.handleErrorWith
import arrow.syntax.function.pipe

typealias IOEither<Left, Right> = EitherT<ForIO, Left, Right>

fun <Left, RightA, RightB> IOEither<Left, RightA>.flatMap(f: (RightA) -> IOEither<Left, RightB>) =
    this.flatMap(IO.monad(), f)

fun <LeftA, LeftB, Right> IOEither<LeftA, Right>.mapLeft(f: (LeftA) -> LeftB) =
    this.mapLeft(IO.functor(), f)

fun <Left, Right> liftEither(either: Either<Left, Right>): IOEither<Left, Right> =
    EitherT.fromEither(IO.applicative(), either)

fun <Left, Right> IOEither<Left, Right>.run(): IO<Either<Left, Right>> =
    this.value().fix()

fun <Left, Right> captureIO(f: suspend () -> Right): IOEither<Left, Right> =
    IO { f() }.map(::right).pipe { EitherT.invoke(it) }

fun <Left, Right> IOEither<Left, Right>.onException(toLeft: (Throwable) -> Left): IOEither<Left, Right> =
    this.handleErrorWith(IO.applicativeError()) { toLeft(it).pipe(::liftLeft) }

private fun <L, R> liftLeft(l: L): IOEither<L, R> = EitherT.left(IO.applicative(), l)
