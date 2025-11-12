ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.13.16"
ThisBuild / organization := "io.github.unknown030405"

lazy val core = (project in file("core"))
  .settings(
    name := "minesweeper-core"
  )
