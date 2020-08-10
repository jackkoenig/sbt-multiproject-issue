

lazy val commonSettings = Seq(
  organization := "com.github.jackkoenig",
  version := "0.1.0",
  scalaVersion := "2.12.11",
  scalacOptions ++= Seq("-deprecation", "-feature")
)

// Optionally depend on bar from source
val barDirOpt = sys.props.get("foo.bar.path")
lazy val bar = project in file(barDirOpt.getOrElse(".fake_bar"))

def dependOnBar(proj: Project): Project = barDirOpt match {
  case None    => proj.settings(libraryDependencies += "com.github.jackkoenig" % "bar" % "1.0.0")
  case Some(_) => proj.dependsOn(bar)
}

lazy val foo = dependOnBar(project in file("."))
  .settings(commonSettings: _*)
