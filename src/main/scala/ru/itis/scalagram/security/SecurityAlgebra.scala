package ru.itis.scalagram.security

import cats.data.EitherT
import ru.itis.scalagram.users.User


trait SecurityAlgebra[F[_], Cred] {
  def auth(cred: Cred): EitherT[F, SecurityError, User]

  def createCred(user: User): EitherT[F, SecurityError, Cred]
}

object SecurityAlgebra {
  def apply[F[_], Cred](implicit sa: SecurityAlgebra[F, Cred]): SecurityAlgebra[F, Cred] = sa
}
