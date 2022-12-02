package com.tudux

import akka.actor.{ActorRef, ActorSystem}
import akka.http.scaladsl.Http
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

  def startHttpServer(shardedParentCostActor: ActorRef)(implicit system: ActorSystem): Unit = {
    implicit val scheduler: ExecutionContext = system.dispatcher

    val routes = new MainRouter(shardedParentCostActor).routes

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


  def setupAdmin(authActor: ActorRef): Unit = {
    val properties = new Properties
    properties.load(getClass.getResourceAsStream("/admin.credentials"))
    val user = properties.get("user").toString
    val password = properties.get("password").toString
    authActor ! CreateSuperUser(User(user,password, AuthRoles.Admin))
  }

  def main(args: Array[String]): Unit = {
    implicit val system  = ActorSystem("jwt-test-system", ConfigFactory.load().getConfig("postgresConfig"))
    val authActor = system.actorOf(AuthenticationActor.props)
    setupAdmin(authActor)
    startHttpServer(authActor)

  }
}
