package com.tudux.api

import akka.actor.{ActorRef, ActorSystem}
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.model.headers.RawHeader
import akka.http.scaladsl.server.Directives.{entity, _}
import akka.http.scaladsl.server.Route
import akka.pattern.ask
import akka.util.Timeout
import com.tudux.actors.AuthActorCommand.ValidateUser

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

case class LoginRouter(authActor: ActorRef)(implicit system: ActorSystem, dispatcher: ExecutionContext,timeout: Timeout )  extends SprayJsonSupport {

  //final case class LoginRequest(username: String, password: String)
  import com.tudux.api.auth.Authorization._
  import com.tudux.api.auth.SecurityDomain._

  def checkPassword(username: String, password: String): Future[Either[String,String]] = {
    (authActor ? ValidateUser(username, password)).mapTo[Either[String,String]]
  }

  val routes: Route = post {
    entity(as[LoginRequest]) { request =>
      onSuccess(checkPassword(request.username,request.password)) {
        case Right(_) =>
          val token = createToken(request.username, 1)
          respondWithHeader(RawHeader("Access-Token", token)) {
            complete(StatusCodes.OK)
          }
        case Left(_) =>
          complete(StatusCodes.Unauthorized)
      }

      /*
      case lr @ LoginRequest(username, password) if checkPassword(username,password) =>
        val token = createToken(username, 1)
        respondWithHeader(RawHeader("Access-Token", token)) {
          complete(StatusCodes.OK)
        }
      case LoginRequest(_, _) => complete(StatusCodes.Unauthorized)
      */

    }
  }

}
