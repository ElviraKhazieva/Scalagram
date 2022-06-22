package ru.itis.scalagram.builders

import cats.Functor
import cats.effect._
import cats.implicits.toSemigroupKOps
import org.http4s.blaze.server.BlazeServerBuilder
import org.http4s.server.{Router, Server}
import ru.itis.scalagram.config.ScalagramConfig
import ru.itis.scalagram.notes.repositories._
import ru.itis.scalagram.notes.routes.NotesEndpoints
import ru.itis.scalagram.notes.{NotebookAlgebra, NotebookInterpreter}
import ru.itis.scalagram.security.repository.TokenRepositoryInterpreter
import ru.itis.scalagram.security.routes.SecurityEndpoints
import ru.itis.scalagram.security.{AccessTokenSecurityInterpreter, Middleware, PasswordSecurityInterpreter}
import ru.itis.scalagram.users.{UserRepositoryAlgebra, UserRepositoryInterpreter, UserValidatorImpl}
import tofu.logging.Logging

import scala.io.Source

class ServerBuilder[F[_]: Async : Sync : Logging.Make : Functor] {

  private implicit val logging: Logging[F] = Logging.Make[F].byName("Server")

  def getBanner: Seq[String] = {
    Source.fromResource("banner.txt").getLines().toSeq
  }

  def buildServer: Resource[F, Server] =
    for {
      conf <- ScalagramConfig.getConfig
      _ <- Resource.eval(DatabaseServicesBuilder.migrateDatabase(conf.db))
      transactor <- DatabaseServicesBuilder.transactor[F](conf.db)
      implicit0(userRepository: UserRepositoryAlgebra[F]) = new UserRepositoryInterpreter[F](transactor)
      implicit0(tokenRepository: TokenRepositoryInterpreter[F]) = new TokenRepositoryInterpreter[F](transactor)
      implicit0(passwordSecurityAlgebra: PasswordSecurityInterpreter[F]) = new PasswordSecurityInterpreter[F]
      implicit0(tokenSecurityAlgebra: AccessTokenSecurityInterpreter[F]) = new AccessTokenSecurityInterpreter[F]
      securityEndpoints = new SecurityEndpoints[F](new UserValidatorImpl)
      implicit0(nra: NotebookRepositoryAlgebra[F]) = new NotebookRepositoryInterpreter[F](transactor)
      implicit0(notera: NoteRepositoryAlgebra[F]) = new NoteRepositoryInterpreter[F](transactor)
      implicit0(rra: RoleRepositoryAlgebra[F]) = new RoleRepositoryInterpreter[F](transactor)
      implicit0(na: NotebookAlgebra[F]) = new NotebookInterpreter[F]
      authMiddleware = new Middleware[F].authMiddleware
      notesEndpoints = new NotesEndpoints[F]
      httpApp = Router(
        "/" -> (securityEndpoints.getEndpoints <+> authMiddleware(notesEndpoints.notesEndpoints()))
      ).orNotFound
      server <- BlazeServerBuilder[F]
        .bindHttp(port = conf.http.port)
        .withBanner(getBanner)
        .withHttpApp(httpApp)
        .resource
    } yield server


}
