package ru.itis.scalagram.security.routes

import cats.data.EitherT
import cats.{Applicative, Functor, Monad}
import cats.syntax.functor._
import cats.effect.kernel.Concurrent
import cats.implicits.{catsSyntaxApply, toFlatMapOps, toSemigroupKOps}
import io.circe.Decoder
import io.circe.generic.semiauto.deriveDecoder
import org.http4s.{EntityDecoder, HttpRoutes}
import org.http4s.circe.jsonOf
import org.http4s.dsl.Http4sDsl
import org.http4s.headers.Authorization
import org.postgresql.util.PSQLException
import org.typelevel.ci.CIString
import ru.itis.scalagram.security.PasswordEncoderAlgebra.PasswordEncoderOps
import ru.itis.scalagram.security.{AccessToken, Password, PasswordEncoderAlgebra, SecurityAlgebra, SecurityError}
import ru.itis.scalagram.users.{LoginOccupied, User, UserAlgebra, UserValidateError, UserValidator}
import tofu.Handle
import tofu.logging._
import tofu.syntax.handle.HandleOps
import tofu.syntax.logging._

import java.util.UUID

class SecurityEndpoints[F[_]
: Monad
: SecurityAlgebra[*[_], Password]
: SecurityAlgebra[*[_], AccessToken]
: Concurrent
: Functor
: PasswordEncoderAlgebra
: UserAlgebra
: Logging
: Handle[*[_], PSQLException]
]
(userValidator: UserValidator) extends Http4sDsl[F] {

  def signUpEndpoint: HttpRoutes[F] = {
    HttpRoutes.of[F] {
      case req@POST -> Root / "signUp" => req
        .as[SignUpForm]
        .flatMap(processSignUpForm(_).value)
        .handleWith(
          psqlHandler[F, PSQLException])
        .flatMap {
          case Left(value) => BadRequest(value.message)
          case Right(_) => Ok()
        }
    }
  }

  def signInEndpoint: HttpRoutes[F] = {
    HttpRoutes.of[F] {
      case req@POST -> Root / "signIn" => req
        .as[SignInForm]
        .flatMap(processSignInForm(_).value)
        .flatMap {
          case Left(value) => BadRequest(value.message)
          case Right(value) => Ok(value)
        }
    }
  }

  def authCheckEndpoint: HttpRoutes[F] = {
    HttpRoutes.of[F] {
      case req@GET -> Root / "authCheck" => req.headers.get(CIString("X-Auth-Token"))
        .map(_.head.value)
        .map(token => {
          SecurityAlgebra[F, AccessToken].auth(AccessToken(UUID.fromString(token))).value
            .flatMap({
              case Left(value) => BadRequest(value.message)
              case Right(value) => Ok()
            })
        }).getOrElse(BadRequest("No token"))
    }
  }

  def psqlHandler[F0[_] : Applicative, E]: E => F0[Either[UserValidateError, Unit]] =
    _ => EitherT.leftT[F0, Unit](LoginOccupied().asInstanceOf[UserValidateError]).value


  def processSignInForm(form: SignInForm): EitherT[F, SecurityError, AccessToken] = {
    SecurityAlgebra[F, Password]
      .auth(Password(form.login, form.password))
      .flatMap(SecurityAlgebra[F, AccessToken].createCred(_))
  }

  def processSignUpForm(form: SignUpForm): EitherT[F, UserValidateError, Unit] = {
    EitherT.fromEither(
      userValidator
        .validateLogin(User(0, form.login, ""))
        .flatMap(userValidator.validatePassword(_, form.password))
    ).flatMap(user =>
      EitherT.right(
        form.password
          .encode[F]
          .map(hash => user.copy(passwordHash = hash))
          .flatMap(UserAlgebra[F].saveUser)
          .flatMap(_ => info"New user added: $user"))
    )
  }

  case class SignUpForm(login: String, password: String)

  case class SignInForm(login: String, password: String)

  implicit val signUpFormDecoder: Decoder[SignUpForm] = deriveDecoder

  implicit val signInFormDecoder: Decoder[SignInForm] = deriveDecoder

  implicit val signUpFormEntityDecoder: EntityDecoder[F, SignUpForm] = jsonOf

  implicit val signInFormEntityDecoder: EntityDecoder[F, SignInForm] = jsonOf

  def getEndpoints: HttpRoutes[F] = {
    signUpEndpoint <+> signInEndpoint <+> authCheckEndpoint
  }

}
