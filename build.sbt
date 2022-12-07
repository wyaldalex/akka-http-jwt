name := "akka-http"

version := "0.1"

scalaVersion := "2.12.12"

val akkaVersion = "2.6.9"
val akkaHttpVersion = "10.2.8"
val scalaTestVersion = "3.2.14"
val postgresVersion = "42.2.2"

libraryDependencies ++= Seq(
  // akka streams
  "com.typesafe.akka" %% "akka-stream" % akkaVersion,
  // akka http
  "com.typesafe.akka" %% "akka-http" % akkaHttpVersion,
  "com.typesafe.akka" %% "akka-http-spray-json" % akkaHttpVersion,
  "com.typesafe.akka" %% "akka-http-testkit" % akkaHttpVersion,
  // testing
  "com.typesafe.akka" %% "akka-testkit" % akkaVersion,
  "org.scalatest" %% "scalatest" % scalaTestVersion,

  // JWT
  "com.pauldijou" %% "jwt-spray-json" % "2.1.0",

  //akka persistence
  "com.typesafe.akka" %% "akka-persistence" % akkaVersion,

// JDBC with PostgreSQL
"org.postgresql" % "postgresql" % postgresVersion,
"com.github.dnvriend" %% "akka-persistence-jdbc" % "3.4.0",
 "com.typesafe.akka" %% "akka-persistence-query" % akkaVersion,

  // Encryption
  "org.mindrot" % "jbcrypt" % "0.4",

  //cats effect for file reading
  "org.typelevel" %% "cats-effect" % "3.3.13"

)