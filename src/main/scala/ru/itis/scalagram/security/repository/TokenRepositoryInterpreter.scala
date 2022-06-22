package ru.itis.scalagram.security.repository

import cats.data.OptionT
import cats.effect.MonadCancelThrow
import cats.effect.std.UUIDGen
import cats.syntax.flatMap._
import cats.syntax.functor._
import cats.{Functor, Monad}
import doobie.implicits._
import doobie.util.transactor.Transactor
import ru.itis.scalagram.security.AccessToken
import ru.itis.scalagram.users.User

class TokenRepositoryInterpreter[F[_] : MonadCancelThrow : UUIDGen : Functor : Monad](val transactor: Transactor[F]) extends TokenRepositoryAlgebra[F] {

  def insert(userId: Long, accessToken: String): F[Unit] =
    sql"INSERT INTO access_token(value, user_id) VALUES ($accessToken, $userId)"
      .update.run.transact(transactor).as({})

  override def createToken(user: User): F[AccessToken] = for {
    uuid <- UUIDGen[F].randomUUID
    _ <- insert(user.id, uuid.toString)
  } yield AccessToken(uuid)

  def select(token: String): F[Option[User]] = (sql"WITH user_ids AS(SELECT user_id FROM access_token WHERE value = $token) " ++
    sql"SELECT * FROM account WHERE id IN (SELECT * FROM user_ids)").query[User].option.transact(transactor)

  override def getUserByToken(token: AccessToken): OptionT[F, User] =
    OptionT(select(token.token.toString))
}
