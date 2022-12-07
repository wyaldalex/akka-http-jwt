package com.tudux

import akka.actor.{ActorRef, ActorSystem}
import akka.http.scaladsl.Http
import cats.effect.IO
import cats.effect.unsafe.implicits.global
import com.tudux.actors.AuthActorCommand.CreateSuperUser
import com.tudux.actors.{AuthRoles, AuthenticationActor, User}
import com.tudux.api.MainRouter
import com.typesafe.config.ConfigFactory

import java.io.FileNotFoundException
import java.util.Properties
import scala.concurrent.ExecutionContext
import scala.io.Source
import scala.util.{Failure, Success}

object Boot {

  def startHttpServer(shardedParentCostActor: ActorRef,secretKey: String)(implicit system: ActorSystem): Unit = {
    implicit val scheduler: ExecutionContext = system.dispatcher

    val routes = new MainRouter(shardedParentCostActor,secretKey).routes

    val bindingFuture = Http().newServerAt("localhost", 8000).bind(routes)

    bindingFuture.onComplete {
      case Success(binding) =>
        val address = binding.localAddress
        system.log.info(s"Server online at http:// ${address.getHostString}:${address.getPort}")

      case Failure(exception) =>
        system.log.error(s"Failed to bing HTTP server, because: $exception")
        system.terminate()
    }
  }


//  def setupAdmin(authActor: ActorRef): Unit = {
//    val properties = new Properties
//    properties.load(getClass.getResourceAsStream("/admin.credentials"))
//    val user = properties.get("user").toString
//    val password = properties.get("password").toString
//    authActor ! CreateSuperUser(User(user,password, AuthRoles.Admin))
//  }

  def processKey(str: String, row: Int): String = {
    str.split("\n")(row).split("=")(1)
  }
  val credentialsFile = "admin.credentials"

  def setupAdminCats(authActor: ActorRef): String = {
    def sourceIO: IO[Source] = IO(Source.fromResource(credentialsFile))
    def readLines(source: Source): IO[String] = IO(source.getLines().mkString("\n"))
    def closeFile(source: Source): IO[Unit] = IO(source.close())

    val bracketRead: IO[String] =
      sourceIO.bracket(src => readLines(src))(src => closeFile(src))

    val res = for {
      x <- bracketRead
      user <- IO(processKey(x, 0))
      password <- IO(processKey(x, 1))
      secretKey <- IO(processKey(x, 2))
      _ <- IO(authActor ! CreateSuperUser(User(user,password, AuthRoles.Admin)))
      _ <- IO(println(s"Super Admin Created with credentials $user/$password"))
    } yield secretKey
    res.unsafeRunSync()
  }

  def main(args: Array[String]): Unit = {
    implicit val system  = ActorSystem("jwt-test-system", ConfigFactory.load().getConfig("postgresConfig"))
    val authActor = system.actorOf(AuthenticationActor.props)
    val secretKey: String = setupAdminCats(authActor)
    startHttpServer(authActor,secretKey)

  }
}
