package ru.itis.scalagram.users

import cats.Functor
import cats.data.OptionT
import cats.effect.MonadCancelThrow
import cats.syntax.functor._
import doobie.implicits._
import doobie.util.transactor.Transactor

class UserRepositoryInterpreter[F[_]: MonadCancelThrow: Functor](val transactor: Transactor[F]) extends UserRepositoryAlgebra[F] {
  override def save(user: User): F[Unit] =
    sql"INSERT INTO account(login, password_hash) VALUES (${user.login}, ${user.passwordHash})"
    .update.withUniqueGeneratedKeys("id").transact(transactor).as(())

  override def getById(id: Long): OptionT[F, User] =
    OptionT(sql"SELECT * FROM account WHERE id=$id".query[User].option.transact(transactor))

  override def getAll: F[List[User]] =
    sql"SELECT * FROM account".query[User].to[List].transact(transactor)

  override def getByLogin(login: String): OptionT[F, User] =
    OptionT(sql"SELECT * FROM account WHERE login = $login".query[User].option.transact(transactor))
}
