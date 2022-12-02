package com.tudux.api

import akka.actor.{ActorRef, ActorSystem}
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.model.headers.RawHeader
import akka.http.scaladsl.server.Directives.{entity, _}
import akka.http.scaladsl.server.Route
import akka.util.Timeout

import scala.concurrent.ExecutionContext

case class LoginRouter(authActor: ActorRef)(implicit system: ActorSystem, dispatcher: ExecutionContext,timeout: Timeout )  extends SprayJsonSupport {

  //final case class LoginRequest(username: String, password: String)
  import com.tudux.api.auth.Authorization._
  import com.tudux.api.auth.SecurityDomain._

  val passwordsSimulatedDB = Map(
    "admin" -> "admin",
    "xor34" -> "12345"
  )

  def checkPassword(username: String, password: String): Boolean = {
    if (passwordsSimulatedDB.contains(username)) {
      passwordsSimulatedDB.get(username) match {
        case Some(credentials) if credentials.equals(password) => true
        case None => false
        case _ => false
      }
    } else false
  }

  val routes: Route = post {
    entity(as[LoginRequest]) {
      case lr @ LoginRequest(username, password) if checkPassword(username,password) =>
        val token = createToken(username, 1)
        respondWithHeader(RawHeader("Access-Token", token)) {
          complete(StatusCodes.OK)
        }
      case LoginRequest(_, _) => complete(StatusCodes.Unauthorized)
    }
  }

}
