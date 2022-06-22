package ru.itis.scalagram

import cats.effect.{ExitCode, IO, IOApp}
import ru.itis.scalagram.builders.ServerBuilder
import tofu.logging.Logging

object Server extends IOApp{

  implicit val makeLogging: Logging.Make[IO] = Logging.Make.plain[IO]

  override def run(args: List[String]): IO[ExitCode] = new ServerBuilder[IO].buildServer.use(_ => IO.never).as(ExitCode.Success)
}
