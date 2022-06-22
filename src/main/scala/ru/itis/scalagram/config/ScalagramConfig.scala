package ru.itis.scalagram.config

import cats.ApplicativeError
import cats.effect.Resource
import io.circe.config.parser

case class ScalagramConfig(db: DatabaseConfig, http: HttpConfig)

object ScalagramConfig {
  def getConfig[F[_]: ApplicativeError[*[_], Throwable]]: Resource[F, ScalagramConfig] = {
    Resource.eval(parser.decodeF[F, ScalagramConfig]())
  }
}
