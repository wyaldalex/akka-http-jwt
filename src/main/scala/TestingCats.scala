import cats.effect.{ExitCode, IO, IOApp}
import cats.effect.unsafe.implicits.global

import java.io.InputStream
import java.util.Properties
import scala.io.Source

//object TestingCats extends IOApp  {
 object TestingCats extends App  {

  //based on: https://www.baeldung.com/scala/read-file-from-resources
  //https://www.baeldung.com/scala/cats-effect-resource-handling

  def processKey(str: String, row: Int): String = {
    str.split("\n")(row).split("=")(1)
  }

  val credentialsFile = "admin.credentials"
  def sourceIO: IO[Source] = IO(Source.fromResource(credentialsFile))
  def readLines(source: Source): IO[String] = IO(source.getLines().mkString("\n"))
  def closeFile(source: Source): IO[Unit] = IO(source.close())

  val bracketRead: IO[String] =
    sourceIO.bracket(src => readLines(src))(src => closeFile(src))
  val res = for {
    x <- bracketRead
    user <- IO(processKey(x,0))
    password <- IO(processKey(x,1))
    secretKey <- IO(processKey(x,2))
    _ <- IO(println(user))
    _ <- IO(println(password))
    _ <- IO(println(secretKey))
  }  yield secretKey
//  val res = for {
//    a <- IO(getClass.getResourceAsStream(credentialsFile))
//    b <- IO(41L)
//    _ <- IO(println(s"Running ioA ${a + b}"))
//  } yield ()

  //this would run outside a Cats App


//  val properties = new Properties
//    properties.load(getClass.getResourceAsStream("/admin.credentials"))
//  val user = properties.get("user").toString
//  val password = properties.get("password").toString

  val x = res.unsafeRunSync()
  println(x.getClass)
  println(x)
  //override def run(args: List[String]): IO[ExitCode] = res.as(ExitCode.Success)
}
