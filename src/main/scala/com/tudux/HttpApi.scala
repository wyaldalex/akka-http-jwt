package com.tudux

import java.util.concurrent.TimeUnit
import akka.actor.{Actor, ActorLogging, Props}
import akka.http.scaladsl.Http
import akka.http.scaladsl.Http.ServerBinding
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.{HttpResponse, StatusCodes}
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.model.headers.RawHeader
import akka.http.scaladsl.server.{Directive1, Route}
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer
import akka.pattern._
import pdi.jwt.{JwtAlgorithm, JwtClaim, JwtSprayJson}
import spray.json._

import scala.util.{Failure, Success}

object SecurityDomain extends DefaultJsonProtocol {
  case class LoginRequest(username: String, password: String)
  implicit val loginRequestFormat = jsonFormat2(LoginRequest)
}

object HttpApi extends SprayJsonSupport {

  final val Name                  = "http-api"
  final val AccessTokenHeaderName = "X-Access-Token"

  //final case class LoginRequest(username: String, password: String)
  import SecurityDomain._

  private val tokenExpiryPeriodInDays = 1
  private val secretKey               = "super_secret_key"
  val algorithm = JwtAlgorithm.HS256


  val passwordsSimulatedDB = Map(
    "admin" -> "admin",
    "xor34" -> "12345"
  )

  def createToken(username: String, expirationPeriodInDays: Int): String = {
    val claims = JwtClaim(
      expiration = Some(System.currentTimeMillis() / 1000 + TimeUnit.DAYS.toSeconds(expirationPeriodInDays)),
      issuedAt = Some(System.currentTimeMillis() / 1000),
      issuer = Some("rockthejvm.com")
    )

    JwtSprayJson.encode(claims, secretKey, algorithm) // JWT string
  }

  def checkPassword(username: String, password: String): Boolean = {
    if (passwordsSimulatedDB.contains(username)) {
      passwordsSimulatedDB.get(username) match {
        case Some(credentials) if credentials.equals(password) => true
        case None => false
        case _ => false
      }
    } else false
  }

  private def login: Route = post {
    entity(as[LoginRequest]) {
      case lr @ LoginRequest(username, password) if checkPassword(username,password) =>
        val token = createToken(username, 1)
        respondWithHeader(RawHeader("Access-Token", token)) {
          complete(StatusCodes.OK)
        }
      case LoginRequest(_, _) => complete(StatusCodes.Unauthorized)
    }
  }

  val authenticatedRoute =
    (path("secureEndpoint") & get) {
      optionalHeaderValueByName("Authorization") {
        case Some(token) =>
          if (isTokenValid(token)) {
            if (isTokenExpired(token)) {
              complete(HttpResponse(status = StatusCodes.Unauthorized, entity = "Token expired."))
            } else {
              complete("User accessed authorized endpoint!")
            }
          } else {
            complete(HttpResponse(status = StatusCodes.Unauthorized, entity = "Token is invalid, or has been tampered with."))
          }
        case _ => complete(HttpResponse(status = StatusCodes.Unauthorized, entity = "No token provided!"))
      }
    }

  def isTokenExpired(token: String): Boolean = JwtSprayJson.decode(token, secretKey, Seq(algorithm)) match {
    case Success(claims) => claims.expiration.getOrElse(0L) < System.currentTimeMillis() / 1000
    case Failure(_) => true
  }

  def isTokenValid(token: String): Boolean = JwtSprayJson.isValid(token, secretKey, Seq(algorithm))


  def routes: Route = login ~ authenticatedRoute

  def apply(host: String, port: Int) = Props(new HttpApi(host, port))
}

final class HttpApi(host: String, port: Int) extends Actor with ActorLogging {
  import HttpApi._
  import context.dispatcher

  private implicit val materializer: ActorMaterializer = ActorMaterializer()

  Http(context.system).bindAndHandle(routes, host, port).pipeTo(self)

  override def receive: Receive = {
    case ServerBinding(address) =>
      log.info("Server successfully bound at {}:{}", address.getHostName, address.getPort)
    case Failure(cause) =>
      log.error("Failed to bind server", cause)
      context.system.terminate()
  }
}
