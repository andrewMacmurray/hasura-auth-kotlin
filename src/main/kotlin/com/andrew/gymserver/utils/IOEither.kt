package com.andrew.gymserver.utils

import arrow.core.Either
import arrow.fx.ForIO
import arrow.fx.IO
import arrow.fx.extensions.io.applicative.applicative
import arrow.fx.extensions.io.functor.functor
import arrow.fx.extensions.io.monad.monad
import arrow.mtl.EitherT

typealias IOEither<Left, Right> = EitherT<ForIO, Left, Right>

fun <Left, RightA, RightB> IOEither<Left, RightA>.flatMap(f: (RightA) -> IOEither<Left, RightB>) =
    this.flatMap(IO.monad(), f)

fun <LeftA, LeftB, Right> IOEither<LeftA, Right>.mapLeft(f: (LeftA) -> LeftB) =
    this.mapLeft(IO.functor(), f)

fun <Left, Right> liftEither(either: Either<Left, Right>): IOEither<Left, Right> =
    EitherT.fromEither(IO.applicative(), either)