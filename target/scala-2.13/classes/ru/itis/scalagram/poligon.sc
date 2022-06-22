import cats.effect.IO
import cats.effect.unsafe.implicits._
import ru.itis.scalagram.security.PasswordEncoderAlgebra

val encoder = PasswordEncoderAlgebra[IO]
encoder.encode("12345678").unsafeRunSync()
encoder.check("12345678", "$2a$10$Q2G6SWokJef4rsMEZyU5JunDK8fKk4/Svu24nfQupMg.arygqmbOe")