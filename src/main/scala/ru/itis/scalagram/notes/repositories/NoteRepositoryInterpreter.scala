package ru.itis.scalagram.notes.repositories

import cats.Functor
import cats.effect.MonadCancelThrow
import cats.syntax.functor._
import doobie.implicits._
import doobie.util.transactor.Transactor
import ru.itis.scalagram.notes.Note

class NoteRepositoryInterpreter[F[_] : MonadCancelThrow : Functor](val transactor: Transactor[F]) extends NoteRepositoryAlgebra[F] {

  def insert(title: String, notebookId: Long, content: String): doobie.Update0 =
    sql"INSERT INTO note(title, notebook_id, note_content) VALUES($title, $notebookId, $content)".update

  def update(id: Long, title: String, content: String): doobie.Update0 =
    sql"UPDATE note SET title = $title, note_content = $content WHERE id=$id".update

  def selectById(noteId: Long): doobie.Query0[Note] =
    sql"SELECT * FROM note WHERE id=$noteId".query[Note]

  def selectByNotebookId(notebookId: Long): doobie.Query0[Note] =
    sql"SELECT * FROM note WHERE notebook_id=$notebookId".query[Note]

  def deleteById(noteId: Long): doobie.Update0  =
    sql"DELETE FROM note WHERE id=$noteId".update

  override def save(note: Note): F[Unit] =
    insert(note.title, note.notebookId, note.content).run.transact(transactor).as(())

  override def update(note: Note): F[Note] =
    update(note.id, note.title, note.content).withUniqueGeneratedKeys[Note]().transact(transactor)

  override def getById(noteId: Long): F[Option[Note]] =
    selectById(noteId).option.transact(transactor)

  override def getByNotebookId(notebookId: Long): F[List[Note]] =
    selectByNotebookId(notebookId).to[List].transact(transactor)

  override def delete(noteId: Long): F[Unit] =
    deleteById(noteId).run.transact(transactor).as(())
}
