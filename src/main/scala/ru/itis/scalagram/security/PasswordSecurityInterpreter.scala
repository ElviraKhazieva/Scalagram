package ru.itis.scalagram.security

import cats.Applicative
import cats.data.EitherT
import cats.syntax.functor._
import ru.itis.scalagram.users.{User, UserRepositoryAlgebra}

class PasswordSecurityInterpreter[F[_] : Applicative : UserRepositoryAlgebra : PasswordEncoderAlgebra] extends SecurityAlgebra[F, Password] {
  override def auth(cred: Password): EitherT[F, SecurityError, User] = {
    EitherT(
      UserRepositoryAlgebra[F]
        .getByLogin(cred.login)
        .value
        .map(checkPassword(_, cred))
    )
  }

  override def createCred(user: User): EitherT[F, SecurityError, Password] = EitherT.fromEither(Left(new PasswordCreationNotSupported))

  def checkPassword(user: Option[User], password: Password): Either[SecurityError, User] = user match {
    case None => Left(new UserNotFound)
    case Some(value) if PasswordEncoderAlgebra[F].check(password.password, value.passwordHash) => Right(value)
    case Some(_) => Left(new CredNotValid)
  }
}
