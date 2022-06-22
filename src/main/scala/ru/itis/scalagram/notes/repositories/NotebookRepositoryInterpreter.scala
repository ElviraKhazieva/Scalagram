package ru.itis.scalagram.notes.repositories

import cats.Functor
import cats.effect.MonadCancelThrow
import cats.syntax.functor._
import doobie.implicits._
import doobie.util.transactor.Transactor
import ru.itis.scalagram.notes.Notebook
import ru.itis.scalagram.users.User

class NotebookRepositoryInterpreter[F[_] : MonadCancelThrow : Functor](val transactor: Transactor[F]) extends NotebookRepositoryAlgebra[F] {

  def insert(name: String, ownerId: Long): doobie.Update0 =
    sql"INSERT INTO notebook (book_name, book_owner) VALUES($name, $ownerId)".update

  def update(id: Long, name: String): doobie.Update0 =
    sql"UPDATE notebook SET book_name = $name WHERE id=$id".update

  def selectById(notebookId: Long): doobie.Query0[Notebook] =
    sql"SELECT * FROM notebook WHERE id=$notebookId".query[Notebook]

  def selectByOwnerId(ownerId: Long): doobie.Query0[Notebook] =
    sql"SELECT * FROM notebook WHERE book_owner=$ownerId".query[Notebook]

  def deleteById(notebookId: Long): doobie.Update0  =
    sql"DELETE FROM notebook WHERE id=$notebookId".update

  override def save(notebook: Notebook, user: User): F[Notebook] =
    insert(notebook.name, user.id).withUniqueGeneratedKeys[Notebook]("id").transact(transactor)

  override def update(notebook: Notebook): F[Notebook] =
    update(notebook.id, notebook.name).withUniqueGeneratedKeys[Notebook]().transact(transactor)

  override def getById(notebookId: Long): F[Option[Notebook]] =
    selectById(notebookId).option.transact(transactor)

  override def delete(notebookId: Long): F[Unit] =
    deleteById(notebookId).run.transact(transactor).as(())

  override def getUserNotebooks(userId: Long): F[List[Notebook]] =
    selectByOwnerId(userId).to[List].transact(transactor)
}
