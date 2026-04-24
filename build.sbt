import org.typelevel.scalacoptions.ScalacOptions

Global / onChangedBuildSource := ReloadOnSourceChanges

ThisBuild / scalaVersion := "3.3.7"
ThisBuild / version      := "0.1.0-SNAPSHOT"

ThisBuild / semanticdbEnabled := true
ThisBuild / semanticdbVersion := scalafixSemanticdb.revision

ThisBuild / scalafmtOnCompile := true

ThisBuild / run / fork := true

lazy val root = project
  .in(file("."))
  .aggregate(http4sGrpc)
  .settings(
    name := "http4s-grpc-playground"
  )

def GrpcProject(projectName: String) =
  Project(projectName, file(projectName)).settings(compiler)

lazy val http4sGrpc = GrpcProject("http4s-grpc")
  .enablePlugins(Http4sGrpcPlugin)
  .settings(
    name := "http4s-grpc",
    Compile / PB.targets ++= Seq(
      scalapb.gen(grpc = false) -> (Compile / sourceManaged).value / "scalapb"
    ),
    libraryDependencies ++= Seq(
      "org.http4s"           %% "http4s-ember-server" % "0.23.34",
      "org.http4s"           %% "http4s-ember-client" % "0.23.34",
      "com.thesamet.scalapb" %% "scalapb-runtime"     % scalapb.compiler.Version.scalapbVersion % "protobuf",
      "ch.qos.logback"        % "logback-classic"     % "1.5.32"                                % Runtime
    )
  )

lazy val compiler: Seq[Def.Setting[?]] = Seq(
  tpolecatScalacOptions ++= Set(
    ScalacOptions.other("-no-indent"),
    ScalacOptions.other("-old-syntax"),
    ScalacOptions.warnOption("conf:src=src_managed/.*:silent")
  ),
  Test / tpolecatExcludeOptions ++= Set(
    ScalacOptions.warnNonUnitStatement
  )
)
