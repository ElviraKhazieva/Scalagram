package ru.itis.scalagram.notes

import cats.implicits.{catsSyntaxApply, catsSyntaxFlatMapOps, toFlatMapOps}
import cats.{Applicative, ApplicativeError, FlatMap, Functor, Monad, MonadError}
import cats.syntax.functor._
import ru.itis.scalagram.notes.repositories.{NoteRepositoryAlgebra, NotebookRepositoryAlgebra, RoleRepositoryAlgebra}
import ru.itis.scalagram.users.User
import tofu.Raise

class NotebookInterpreter
[F[_]
: NoteRepositoryAlgebra
: NotebookRepositoryAlgebra
: RoleRepositoryAlgebra
: Functor
: Applicative
: Monad
: Raise[*[_], AccessError]
] extends NotebookAlgebra[F] {

  override def create(user: User, title: String): F[Unit] =
    NotebookRepositoryAlgebra[F].save(Notebook(0, title, user.id), user).as(())

  override def edit(user: User, notebookId: Long, newTitle: String): F[Unit] =
    checkAccess(user, notebookId, writeAccess = true) >>
      NotebookRepositoryAlgebra[F].update(Notebook(notebookId, newTitle, 0)).as(())

  override def delete(user: User, notebookId: Long): F[Unit] =
    checkAccess(user, notebookId, writeAccess = true) >>
      NotebookRepositoryAlgebra[F].delete(notebookId)

  override def addNote(user: User, note: Note): F[Unit] =
    checkAccess(user, note.notebookId, writeAccess = true) >>
      NoteRepositoryAlgebra[F].save(note)

  override def editNote(user: User, note: Note): F[Unit] =
    checkAccess(user, note.notebookId, writeAccess = true) >>
      NoteRepositoryAlgebra[F].update(note).as(())

  override def deleteNote(user: User, note: Note): F[Unit] =
    checkAccess(user, note.notebookId, writeAccess = true) >>
      NoteRepositoryAlgebra[F].delete(note.id)

  override def getUserNotebooks(user: User): F[List[Notebook]] =
    NotebookRepositoryAlgebra[F].getUserNotebooks(user.id)

  override def getNotebookNotes(user: User, notebookId: Long): F[List[Note]] =
    checkAccess(user, notebookId, writeAccess = false) >>
      NoteRepositoryAlgebra[F].getByNotebookId(notebookId)

  override def addRole(user: User, anotherUser: User, notebookId: Long, role: Role): F[Unit] =
    checkAccess(user, notebookId, writeAccess = true) >>
      RoleRepositoryAlgebra[F].addRole(user.id, notebookId, role)

  override def removeRole(user: User, anotherUser: User, notebookId: Long): F[Unit] =
    checkAccess(user, notebookId, writeAccess = true) >>
      RoleRepositoryAlgebra[F].removeRole(user.id, notebookId)

  override def changeRole(user: User, anotherUser: User, notebookId: Long, role: Role): F[Unit] =
    removeRole(user, anotherUser, notebookId) >>
      RoleRepositoryAlgebra[F].addRole(user.id, notebookId, role)

  private def checkAccess(user: User, notebookId: Long, writeAccess: Boolean): F[Boolean] =
    RoleRepositoryAlgebra[F].getRole(user.id, notebookId).map {
      case None => false
      case Some(value) if value == Reader && writeAccess => false
      case _ => true
    }.flatMap { value =>
      isOwner(user, notebookId).map(if (_) true else value)
    }.flatMap{
      case true => Applicative[F].pure(true)
      case false => Raise[F, AccessError].raise(new AccessError)
    }

  private def isOwner(user: User, notebookId: Long): F[Boolean] = {
    NotebookRepositoryAlgebra[F].getById(notebookId).map {
      case Some(value) => value.ownerId == user.id
      case None => false
    }
  }

}
