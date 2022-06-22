package ru.itis.scalagram.users

trait UserAlgebra[F[_]] {
  def saveUser(user: User): F[Unit]
}

object UserAlgebra {

  def apply[F[_]](implicit ua: UserAlgebra[F]): UserAlgebra[F] = ua

  implicit def defaultUserAlgebra[F[_]](implicit ur: UserRepositoryAlgebra[F]): UserAlgebra[F] = (user: User) => ur.save(user)
}
