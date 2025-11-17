ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.13.16"
ThisBuild / organization := "io.github.unknown030405"

ThisBuild / scalafixDependencies += "org.typelevel" %% "typelevel-scalafix" % "0.5.0"

lazy val core = (project in file("core"))
  .settings(
    name := "minesweeper-core",
    libraryDependencies ++= List(
      "org.scalatest" %% "scalatest" % "3.2.19" % Test
    )
  )

assembly / assemblyMergeStrategy := {
  case PathList("META-INF", xs @ _*) => MergeStrategy.discard
  case x => MergeStrategy.first
}

assembly / mainClass := Some("io.github.unknown030405.minesweeper.gui.MinesweeperApp")

lazy val gui = (project in file("gui"))
  .dependsOn(core)
  .settings(
    assembly / assemblyMergeStrategy := {
      case PathList("META-INF", xs @ _*) => MergeStrategy.discard
      case x => MergeStrategy.first
    },
    name         := "minesweeper-gui",
    organization := "io.github.unknown030405",
//    fork         := true,
    javaOptions ++= {
      val javafxPath = System.getProperty("java.class.path").split(java.io.File.pathSeparator)
        .find(_.contains("javafx"))
        .getOrElse("")
      Seq("--module-path", javafxPath, "--add-modules", "javafx.controls,javafx.fxml")
    },
    libraryDependencies ++= Seq(
      "org.scalatest" %% "scalatest" % "3.2.19" % Test,
      "org.scalafx"   %% "scalafx"   % "24.0.2-R36",
    )
  )
