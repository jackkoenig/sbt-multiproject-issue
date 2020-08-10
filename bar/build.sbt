

lazy val commonSettings = Seq(
  organization := "com.github.jackkoenig",
  version := "0.1.0",
  scalaVersion := "2.12.11",
  scalacOptions ++= Seq("-deprecation", "-feature"),
  libraryDependencies += "com.lihaoyi" %% "upickle" % "0.9.5"
)

lazy val bar = (project in file("."))
  .settings(commonSettings: _*)
