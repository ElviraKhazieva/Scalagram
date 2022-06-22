package ru.itis.scalagram.notes.routes

import cats.effect.Concurrent
import cats.implicits.toFlatMapOps
import cats.{Monad, MonadError}
import io.circe.Decoder
import io.circe.generic.semiauto.deriveDecoder
import org.http4s.circe.{jsonEncoderOf, jsonOf}
import org.http4s.dsl.Http4sDsl
import org.http4s.{AuthedRoutes, EntityDecoder, EntityEncoder, HttpRoutes, Request, Response}
import ru.itis.scalagram.notes.{AccessError, Note, Notebook, NotebookAlgebra, Role}
import ru.itis.scalagram.users.User
import tofu.Handle
import tofu.syntax.handle._

class NotesEndpoints[F[_]
: Monad
: NotebookAlgebra
: Handle[*[_], AccessError]
: Handle[*[_], NumberFormatException]
: Concurrent
] extends Http4sDsl[F] {
  def notesEndpoints(): AuthedRoutes[User, F] = AuthedRoutes.of[User, F] {
    case GET -> Root / userId / "notebooks" as user =>
      handleAccessRequest(NotebookAlgebra[F].getUserNotebooks(User(userId.toLong, "", "")))

    case GET -> Root / notebookId / "notes" as user =>
      handleAccessRequest(NotebookAlgebra[F].getNotebookNotes(user, notebookId.toLong))

    case req@POST -> Root / notebookId / "addNote" as user =>
      handleAccessRequest(req.req.
        as[AddNoteForm]
        .flatMap(value =>
          NotebookAlgebra[F]
            .addNote(user, Note(0, value.title, notebookId.toLong, value.content))))

    case DELETE -> Root / notebookId / "notes" / noteId / "delete" as user =>
      handleAccessRequest(NotebookAlgebra[F].deleteNote(user, Note(noteId.toLong, "", notebookId.toLong, "")))

    case req@PATCH -> Root / notebookId / "notes" / noteId / "edit" as user =>
      handleAccessRequest(req.req.
        as[EditNoteForm]
        .flatMap(value => NotebookAlgebra[F].editNote(user, Note(noteId.toLong, "", notebookId.toLong, value.content))))

    case req@POST -> Root / "notebooks" / "create" as user =>
      handleAccessRequest(req.req.
        as[CreateNotebookForm]
        .flatMap(value => NotebookAlgebra[F].create(user, value.name)))

    case req@PATCH -> Root / notebookId / "edit" as user =>
      handleAccessRequest(req.req.
        as[EditNotebookForm]
        .flatMap(value => NotebookAlgebra[F].edit(user, notebookId.toLong, value.name)))

    case DELETE -> Root / notebookId / "delete" as user =>
      handleAccessRequest(NotebookAlgebra[F].delete(user, notebookId.toLong))

    case req@POST -> Root / notebookId / "addRoleTo" / userId as user =>
      handleAccessRequest(req.req.
        as[EditNotebookForm]
        .flatMap(value =>
          NotebookAlgebra[F]
            .changeRole(user, User(userId.toLong, "", ""), notebookId.toLong, Role.fromStr(value.name))))

    case DELETE -> Root / notebookId / "removeRoleOf" / userId as user =>
      handleAccessRequest(NotebookAlgebra[F].removeRole(user, User(userId.toLong, "", ""), notebookId.toLong))



  }

  def handleAccessRequest[A](f: => F[A])(implicit encoder: EntityEncoder[F, A]): F[Response[F]] = {
    f.attempt[AccessError].flatMap {
      case Left(value) => Forbidden()
      case Right(value) => Ok(value)
    }
      .handleWith[NumberFormatException](_ => UnprocessableEntity())
  }

  implicit val addNoteFormDecoder: Decoder[AddNoteForm] = deriveDecoder

  implicit val addNoteFormEntityEncoder: EntityDecoder[F, AddNoteForm] = jsonOf

  case class AddNoteForm(title: String, content: String)

  implicit val editNoteFormDecoder: Decoder[EditNoteForm] = deriveDecoder

  implicit val editNoteFormEntityEncoder: EntityDecoder[F, EditNoteForm] = jsonOf

  case class EditNoteForm(content: String)

  implicit val createNotebookFormDecoder: Decoder[CreateNotebookForm] = deriveDecoder

  implicit val createNotebookFormEntityEncoder: EntityDecoder[F, CreateNotebookForm] = jsonOf

  case class CreateNotebookForm(name: String)

  implicit val editNotebookFormDecoder: Decoder[EditNotebookForm] = deriveDecoder

  implicit val editNotebookFormEntityEncoder: EntityDecoder[F, EditNotebookForm] = jsonOf

  case class EditNotebookForm(name: String)

  implicit val roleFormDecoder: Decoder[RoleForm] = deriveDecoder

  implicit val roleFormEntityEncoder: EntityDecoder[F, RoleForm] = jsonOf

  case class RoleForm(role: String)
}
