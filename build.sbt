//import sbt.Keys.libraryDependencies
import coursierapi.MavenRepository

ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.13.16"
ThisBuild / organization := "io.github.unknown030405"

ThisBuild / scalafixDependencies += "org.typelevel"       %% "typelevel-scalafix" % "0.5.0"

lazy val core = (project in file("core"))
  .settings(
    name := "minesweeper-core",
    libraryDependencies ++= List(
      "org.scalatest" %% "scalatest" % "3.2.19" % Test
    )
  )
