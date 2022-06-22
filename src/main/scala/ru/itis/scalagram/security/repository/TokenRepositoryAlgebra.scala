package ru.itis.scalagram.security.repository

import cats.data.OptionT
import ru.itis.scalagram.security.AccessToken
import ru.itis.scalagram.users.User

trait TokenRepositoryAlgebra[F[_]] {
  def createToken(user: User): F[AccessToken]

  def getUserByToken(token: AccessToken): OptionT[F, User]
}

object TokenRepositoryAlgebra {
  def apply[F[_]](implicit tr: TokenRepositoryAlgebra[F]): TokenRepositoryAlgebra[F] = tr
}