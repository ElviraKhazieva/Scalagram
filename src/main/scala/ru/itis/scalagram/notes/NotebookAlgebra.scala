package ru.itis.scalagram.notes

import cats.data.EitherT
import ru.itis.scalagram.users.User

trait NotebookAlgebra[F[_]] {
  def create(user: User, title: String): F[Unit]

  def edit(user: User, notebookId: Long, newTitle:  String): F[Unit]

  def delete(user: User, notebookId: Long): F[Unit]

  def addNote(user: User, note: Note): F[Unit]

  def editNote(user: User, note: Note): F[Unit]

  def deleteNote(user: User, note: Note): F[Unit]

  def getUserNotebooks(user: User): F[List[Notebook]]

  def getNotebookNotes(user: User, notebookId: Long): F[List[Note]]

  def addRole(user: User, anotherUser: User, notebookId: Long, role: Role): F[Unit]

  def removeRole(user: User, anotherUser: User, notebookId: Long): F[Unit]

  def changeRole(user: User, anotherUser: User, notebookId: Long, role: Role): F[Unit]
}
object NotebookAlgebra {
  def apply[F[_]](implicit notebookAlgebra: NotebookAlgebra[F]): NotebookAlgebra[F] = notebookAlgebra
}
