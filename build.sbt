name := "akka-http"

version := "0.1"

scalaVersion := "2.12.8"

val akkaVersion = "2.5.20"
val akkaHttpVersion = "10.1.7"
val scalaTestVersion = "3.0.5"
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

)