# Issue with Multi-Project builds in SBT

## steps

Just running `sbt compile` in my repo is sufficient. 

```bash
git clone git@github.com:jackkoenig/sbt-multiproject-issue.git
cd sbt-multiproject-issue
sbt compile
```

For more information about my system, here's the output of `sbt -d`:

```
sbt -d
[residual] arg = '-Droot.fizz.path=fizz'
[residual] arg = '-Dfoo.bar.path=../bar'
[residual] arg = '-Dfizz.foo.path=../foo'
[residual] arg = '-Dfizz.bar.path=../bar'
[process_args] java_version = '1.8'
# Executing command line:
java
-Xms1024m
-Xmx1024m
-XX:ReservedCodeCacheSize=128m
-XX:MaxMetaspaceSize=256m
-jar
/usr/share/sbt/bin/sbt-launch.jar
-Droot.fizz.path=fizz
-Dfoo.bar.path=../bar
-Dfizz.foo.path=../foo
-Dfizz.bar.path=../bar

[info] welcome to sbt 1.3.13 (Private Build Java 1.8.0_252)
[info] loading project definition from /scratch/koenig/sbt-build/not-working/project
[info] loading settings for project root from build.sbt ...
[info] loading settings for project fizz from build.sbt ...
[info] loading settings for project foo from build.sbt ...
[info] loading settings for project bar from build.sbt ...
[info] loading settings for project bar from build.sbt ...
[info] set current project to root (in build file:/scratch/koenig/sbt-build/not-working/)
[info] sbt server started at local:///home/koenig/.sbt/1.0/server/36b2d0bca277d3a60a10/sock
sbt:root> compile
[info] Compiling 1 Scala source to /scratch/koenig/sbt-build/not-working/bar/target/scala-2.12/classes ...
[error] /scratch/koenig/sbt-build/not-working/bar/src/main/scala/bar/Bar.scala:4:8: not found: object upickle
[error] import upickle.default._
[error]        ^
[error] /scratch/koenig/sbt-build/not-working/bar/src/main/scala/bar/Bar.scala:10:34: not found: value write
[error]   println(s"Hello from Bar! $b ${write(List(1,2,3))}")
[error]                                  ^
[error] two errors found
[error] (bar / Compile / compileIncremental) Compilation failed
[error] Total time: 1 s, completed Aug 10, 2020 2:36:09 PM
```

## problem

It looks as if because multiple projecs have a common source dependency, that SBT is somehow losing the information about that project.

Some information from SBT shell that illustrates the problem. All projects should have version `0.1.0`:
```
sbt:root> projects
[info] In file:/scratch/koenig/sbt-build/not-working/
[info]     bar
[info]     fizz
[info]     foo
[info]   * root
sbt:root> show version
[info] 0.1.0
sbt:root> project fizz
[info] set current project to fizz (in build file:/scratch/koenig/sbt-build/not-working/)
sbt:fizz> show version
[info] 0.1.0
sbt:fizz> project foo
[info] set current project to foo (in build file:/scratch/koenig/sbt-build/not-working/)
sbt:foo> show version
[info] 0.1.0
sbt:foo> project bar
[info] set current project to bar (in build file:/scratch/koenig/sbt-build/not-working/)
sbt:bar> show version
[info] 0.1.0-SNAPSHOT
sbt:bar> show libraryDependencies
[info] * org.scala-lang:scala-library:2.12.10
```
`bar/build.sbt`:
```scala
lazy val commonSettings = Seq(
  organization := "com.github.jackkoenig",
  version := "0.1.0",
  scalaVersion := "2.12.11",
  scalacOptions ++= Seq("-deprecation", "-feature"),
  libraryDependencies += "com.lihaoyi" %% "upickle" % "0.9.5"
)

lazy val bar = (project in file("."))
  .settings(commonSettings: _*)
```

The project is *aware* of `bar`, but it seems to be ignoring it's details. In more complex examples, if you have other projects in the same `build.sbt`,
they show up and are loaded properly.

## expectation

I would expect `bar` to be handled correctly and not have its configuration ignored.

I think this is a bug because I can work around the issue with some changes, see next section.

## notes

The project structure is as follows:
```
├── build.sbt
├── .sbtopts
├── bar
│   └── build.sbt
├── fizz
│   └── build.sbt
└── foo
    └── build.sbt
```
* `root` depends on `fizz`
* `fizz` depends on `foo` and `bar`
* `foo` depends on `bar`
* `bar` has a library dependency

Importantly, I'm using system properties to make it possible to switch between source and library dependencies.
While this example is a single repo, my real use cases is separate repos for each project that can either be used either as library or source dependencies.

The root directory `.sbtopts` contains the dependency paths for each project:
```
 cat .sbtopts
-Droot.fizz.path=fizz
-Dfoo.bar.path=../bar
-Dfizz.foo.path=../foo
-Dfizz.bar.path=../bar
```
`-Dfizz.foo.path` reads as the relative path from fizz to foo.


### Workaround 1

Remove the dependency from `fizz` on `bar`, instead rely on transitive dependency through `foo`.

```bash
git apply patch1.diff
sbt compile
```

### Workaround 2

Remove `..` from relative paths, instead using soft links.

(Make sure repo is clean from previous workaround)

```bash
cd foo && ln -s ../bar && cd ..
cd fizz && ln -s ../foo && ln -s ../bar && cd ..
sbt compile
git apply patch2.diff
```

This shows that the basic idea can work, and it seems that something about going up and down is causing the issue.

