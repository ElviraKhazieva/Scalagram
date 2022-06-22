package ru.itis.scalagram.notes.repositories

import cats.Functor
import cats.effect.MonadCancelThrow
import cats.syntax.functor._
import doobie.implicits._
import doobie.util.transactor.Transactor
import ru.itis.scalagram.notes.Role
import ru.itis.scalagram.notes.Role.strMapping


class RoleRepositoryInterpreter[F[_] : MonadCancelThrow : Functor](val transactor: Transactor[F]) extends RoleRepositoryAlgebra[F] {

  def insert(userId: Long, notebookId: Long, role: String): doobie.Update0 =
    sql"INSERT INTO roles (user_id, notebook_id, user_role) VALUES($userId, $notebookId, $role)".update

  def selectById(userId: Long, notebookId: Long): doobie.Query0[String] =
    sql"SELECT user_role FROM roles WHERE user_id=$userId AND notebook_id=$notebookId".query[String]

  def deleteById(userId: Long, notebookId: Long): doobie.Update0  =
    sql"DELETE FROM roles WHERE user_id=$userId AND notebook_id=$notebookId".update


  override def addRole(userId: Long, notebookId: Long, role: Role): F[Unit] =
    insert(userId, notebookId, strMapping(role)).run.transact(transactor).as(())

  override def getRole(userId: Long, notebookId: Long): F[Option[Role]] =
    selectById(userId, notebookId).option.transact(transactor).map(_.map(Role.fromStr))

  override def removeRole(userId: Long, notebookId: Long): F[Unit] =
    deleteById(userId, notebookId).run.transact(transactor).as(())
}
