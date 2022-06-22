package ru.itis.scalagram.security

import cats.effect.kernel.Sync
import com.github.t3hnar.bcrypt._

trait PasswordEncoderAlgebra[F[_]] {
  def encode(password: String): F[String]

  def check(password: String, passwordHash: String): Boolean
}

object PasswordEncoderAlgebra {

  def apply[F[_]](implicit pe: PasswordEncoderAlgebra[F]): PasswordEncoderAlgebra[F] = pe

  implicit class PasswordEncoderOps(value: String) {
    def encode[F[_]](implicit pe: PasswordEncoderAlgebra[F]): F[String] = pe.encode(value)

    def check[F[_]](passwordHash: String)(implicit pe: PasswordEncoderAlgebra[F]): Boolean = pe.check(value, passwordHash)
  }

  implicit def defaultPasswordEncoder[F[_] : Sync]: PasswordEncoderAlgebra[F] = new PasswordEncoderAlgebra[F] {
    override def encode(password: String): F[String] = Sync[F].delay(password.boundedBcrypt)

    override def check(password: String, passwordHash: String): Boolean = password.isBcryptedBounded(passwordHash)
  }
}
