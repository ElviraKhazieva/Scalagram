package ru.itis.scalagram.notes.repositories

import ru.itis.scalagram.notes.Note

trait NoteRepositoryAlgebra[F[_]] {
  def save(note: Note): F[Unit]

  def update(note: Note): F[Note]

  def getById(noteId: Long): F[Option[Note]]

  def getByNotebookId(notebookId: Long): F[List[Note]]

  def delete(noteId: Long): F[Unit]
}
object NoteRepositoryAlgebra {
  def apply[F[_]](implicit nr: NoteRepositoryAlgebra[F]): NoteRepositoryAlgebra[F] = nr
}
