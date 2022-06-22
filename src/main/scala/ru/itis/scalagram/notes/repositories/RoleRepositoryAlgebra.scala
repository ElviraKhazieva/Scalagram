package ru.itis.scalagram.notes.repositories

import ru.itis.scalagram.notes.Role

trait RoleRepositoryAlgebra[F[_]] {
  def addRole(userId: Long, notebookId: Long, role: Role): F[Unit]

  def getRole(userId: Long, notebookId: Long): F[Option[Role]]

  def removeRole(userId: Long, notebookId: Long): F[Unit]
}
object RoleRepositoryAlgebra {
  def apply[F[_]](implicit rr: RoleRepositoryAlgebra[F]): RoleRepositoryAlgebra[F] = rr
}
