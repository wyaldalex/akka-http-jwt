package com.tudux.api

import akka.actor.{ActorRef, ActorSystem}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.util.Timeout

import scala.concurrent.ExecutionContext
import scala.concurrent.duration.DurationInt

class MainRouter(authActor: ActorRef, secretKey: String)(implicit system: ActorSystem) {

  implicit val dispatcher: ExecutionContext = system.dispatcher
  implicit val timeout: Timeout = Timeout(30.seconds)

  val authRoutes = LoginRouter(authActor,secretKey).routes
  val authenticatedRoutes = AuthenticatedRouter(secretKey).routes

  val routes: Route = {
    authRoutes ~
    authenticatedRoutes
  }

}
