package ru.itis.scalagram.security

import cats.data.{Kleisli, OptionT}
import cats.effect.Sync
import org.http4s.Request
import org.http4s.server.AuthMiddleware
import org.typelevel.ci.CIString
import ru.itis.scalagram.users.User

import java.util.UUID

class Middleware[F[_] : Sync : SecurityAlgebra[*[_], AccessToken]] {

  val authUser: Kleisli[OptionT[F, *], Request[F], User] =
    Kleisli(req =>
      getAuthToken(req) match {
        case Some(value) => authToken(value)
        case None => OptionT.none[F, User]
      }
    )

  def getAuthToken(request: Request[F]): Option[String] = {
    request.headers.get(CIString("X-Auth-Token"))
      .map(_.head.value)
  }

  def authToken(token: String): OptionT[F, User] = {
    SecurityAlgebra[F, AccessToken].auth(AccessToken(UUID.fromString(token))).toOption
  }

  val authMiddleware: AuthMiddleware[F, User] = AuthMiddleware(authUser)

}
