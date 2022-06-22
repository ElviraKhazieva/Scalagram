package ru.itis.scalagram.security

import cats.{ApplicativeError, Functor}
import cats.syntax.functor._
import cats.syntax.applicativeError._
import cats.data.EitherT
import ru.itis.scalagram.security.repository.TokenRepositoryAlgebra
import ru.itis.scalagram.users.User

class AccessTokenSecurityInterpreter[F[_] : TokenRepositoryAlgebra : Functor : ApplicativeError[*[_], Throwable]] extends SecurityAlgebra[F, AccessToken] {
  override def auth(cred: AccessToken): EitherT[F, SecurityError, User] =
      TokenRepositoryAlgebra[F]
        .getUserByToken(cred)
        .toRight[SecurityError](new UserNotFound)

  override def createCred(user: User): EitherT[F, SecurityError, AccessToken] =
    EitherT(
      TokenRepositoryAlgebra[F]
        .createToken(user)
        .map[Either[SecurityError, AccessToken]](token => Right(token))
        .handleError(_ => Left(new UserNotFound))
    )
}
