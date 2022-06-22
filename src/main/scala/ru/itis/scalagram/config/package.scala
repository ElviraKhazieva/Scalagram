package ru.itis.scalagram

import io.circe.Decoder
import io.circe.generic.semiauto.deriveDecoder

package object config {
  implicit val databaseConfigDecoder: Decoder[DatabaseConfig] = deriveDecoder
  implicit val httpConfigDecoder: Decoder[HttpConfig] = deriveDecoder
  implicit val scalagramConfigDecoder: Decoder[ScalagramConfig] = deriveDecoder
}
