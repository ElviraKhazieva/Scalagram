package ru.itis.scalagram.notes.repositories

import ru.itis.scalagram.notes.Notebook
import ru.itis.scalagram.users.User

trait NotebookRepositoryAlgebra[F[_]] {
  def save(notebook: Notebook, user: User): F[Notebook]

  def update(notebook: Notebook): F[Notebook]

  def getById(noteId: Long): F[Option[Notebook]]

  def delete(notebookId: Long): F[Unit]

  def getUserNotebooks(userId: Long): F[List[Notebook]]
}
object NotebookRepositoryAlgebra {
  def apply[F[_]](implicit nr: NotebookRepositoryAlgebra[F]): NotebookRepositoryAlgebra[F] = nr
}
