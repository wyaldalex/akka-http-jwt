package com.tudux.api.auth

import pdi.jwt.{JwtAlgorithm, JwtClaim, JwtSprayJson}
import spray.json.DefaultJsonProtocol

import java.util.concurrent.TimeUnit
import scala.util.{Failure, Success}


object SecurityDomain extends DefaultJsonProtocol {
  case class LoginRequest(username: String, password: String)
  implicit val loginRequestFormat = jsonFormat2(LoginRequest)
}

object Authorization {

  //private val secretKey = "super_secret_key"
  val algorithm = JwtAlgorithm.HS256

  def createToken(username: String, expirationPeriodInDays: Int, secretKey: String): String = {
    val claims = JwtClaim(
      expiration = Some(System.currentTimeMillis() / 1000 + TimeUnit.DAYS.toSeconds(expirationPeriodInDays)),
      issuedAt = Some(System.currentTimeMillis() / 1000),
      issuer = Some("com.tudux")
    )

    JwtSprayJson.encode(claims, secretKey, algorithm) // JWT string
  }

  def isTokenExpired(token: String,secretKey: String): Boolean = JwtSprayJson.decode(token, secretKey, Seq(algorithm)) match {
    case Success(claims) => claims.expiration.getOrElse(0L) < System.currentTimeMillis() / 1000
    case Failure(_) => true
  }

  def isTokenValid(token: String,secretKey: String): Boolean = JwtSprayJson.isValid(token, secretKey, Seq(algorithm))

}
