ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.13.8"

lazy val root = (project in file("."))
  .settings(
    name := "foresight"
  )

libraryDependencies += "com.github.pureconfig" %% "pureconfig" % "0.17.1"
//Cats
libraryDependencies ++= Seq(
  "org.typelevel" %% "cats-core" % "2.7.0",
)
libraryDependencies ++= Seq(
  "dev.zio" %% "zio" % "1.0.13",
  "dev.zio" %% "zio-interop-cats" % "3.2.9.1",
  "io.scalac" %% "zio-akka-http-interop" % "0.5.0"
)
//Akka
val AkkaVersion = "2.6.19"
libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor-typed" % AkkaVersion,
  "com.typesafe.akka" %% "akka-stream" % AkkaVersion,
)
//Api
val AkkaHttpVersion = "10.2.9"
libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-http" % AkkaHttpVersion,
//  "com.typesafe.akka" %% "akka-http-core" % "10.2.9",
)
//JSON
val circeVersion = "0.14.1"
libraryDependencies ++= Seq(
  "io.circe" %% "circe-core",
  "io.circe" %% "circe-generic",
  "io.circe" %% "circe-parser"
).map(_ % circeVersion)

//libraryDependencies += "de.heikoseeberger" %% "akka-http-circe" % "1.39.2"
//Database
libraryDependencies ++= Seq(
  "com.typesafe.slick" %% "slick" % "3.3.3",
  "org.slf4j" % "slf4j-nop" % "1.7.36", //Remove it if you want database logging
  "com.typesafe.slick" %% "slick-hikaricp" % "3.3.3",
  "mysql" % "mysql-connector-java" % "8.0.28",
)