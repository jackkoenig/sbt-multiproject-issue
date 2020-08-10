

lazy val commonSettings = Seq(
  organization := "com.github.jackkoenig",
  version := "0.1.0",
  scalaVersion := "2.12.11",
  scalacOptions ++= Seq("-deprecation", "-feature")
)

// Optionally depend on fizz from source
val fizzDirOpt = sys.props.get("root.fizz.path")
lazy val fizz = project in file(fizzDirOpt.getOrElse(".fake_fizz"))

def dependOnFizz(proj: Project): Project = fizzDirOpt match {
  case None    => proj.settings(libraryDependencies += "com.github.jackkoenig" % "fizz" % "1.0.0")
  case Some(_) => proj.dependsOn(fizz)
}


lazy val root = dependOnFizz(project in file("."))
  .settings(commonSettings: _*)
