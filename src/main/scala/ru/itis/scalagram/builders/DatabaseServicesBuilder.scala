package ru.itis.scalagram.builders

import cats.{Functor, Monad}
import cats.effect.{MonadCancelThrow, Resource, Sync}
import cats.effect.kernel.Async
import cats.effect.std.UUIDGen
import org.flywaydb.core.Flyway
import cats.syntax.functor._
import doobie.ExecutionContexts
import doobie.hikari.HikariTransactor
import doobie.util.transactor.Transactor
import org.flywaydb.core.api.Location
import ru.itis.scalagram.config.DatabaseConfig
import ru.itis.scalagram.security.repository.TokenRepositoryInterpreter
import ru.itis.scalagram.users.UserRepositoryInterpreter

object DatabaseServicesBuilder {

  def migrateDatabase[F[_] : Sync](databaseConfig: DatabaseConfig): F[Unit] = {
    Sync[F].delay {
      Flyway.configure()
        .dataSource(databaseConfig.url, databaseConfig.user, databaseConfig.password)
        .locations(new Location(databaseConfig.migrationsPath))
        .baselineOnMigrate(true)
        .load()
        .migrate()
    }.as {
      ()
    }
  }

  def transactor[F[_] : Async](databaseConfig: DatabaseConfig): Resource[F, Transactor[F]] =
    for {
      ce <- ExecutionContexts.fixedThreadPool[F](databaseConfig.poolConnections)
      transactor <- HikariTransactor.newHikariTransactor[F](
        databaseConfig.driverClass,
        databaseConfig.url,
        databaseConfig.user,
        databaseConfig.password,
        ce
      )
    } yield transactor


}
