package ru.itis.scalagram.users

import cats.data.OptionT

trait UserRepositoryAlgebra[F[_]] {

  def save(user: User): F[Unit]

  def getById(id: Long): OptionT[F, User]

  def getAll: F[List[User]]

  def getByLogin(login: String): OptionT[F, User]

}
object UserRepositoryAlgebra {
  def apply[F[_]](implicit ur: UserRepositoryAlgebra[F]): UserRepositoryAlgebra[F] = ur
}
