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
  .aggregate(http4sGrpc, fs2Grpc)
  .settings(
    name := "http4s-grpc-playground"
  )

def GrpcProject(projectName: String) =
  Project(projectName, file(projectName))
    .settings(compiler)
    .settings(
      libraryDependencies ++= Seq(
        "ch.qos.logback"        % "logback-classic" % "1.5.32"                                % Runtime,
        "com.thesamet.scalapb" %% "scalapb-runtime" % scalapb.compiler.Version.scalapbVersion % "protobuf"
      )
    )

lazy val http4sGrpc = GrpcProject("http4s-grpc")
  .enablePlugins(Http4sGrpcPlugin)
  .settings(
    name := "http4s-grpc",
    Compile / PB.targets ++= Seq(
      scalapb.gen(grpc = false) -> (Compile / sourceManaged).value / "scalapb"
    ),
    libraryDependencies ++= Seq(
      "org.http4s" %% "http4s-ember-server" % "0.23-786604e-SNAPSHOT",
      "org.http4s" %% "http4s-ember-client" % "0.23-786604e-SNAPSHOT"
    )
  )

lazy val fs2Grpc = GrpcProject("fs2-grpc")
  .enablePlugins(Fs2Grpc)
  .settings(
    name := "fs2-grpc",
    libraryDependencies ++= Seq(
      "io.grpc" % "grpc-netty-shaded" % scalapb.compiler.Version.grpcJavaVersion,
      "io.grpc" % "grpc-services"     % scalapb.compiler.Version.grpcJavaVersion
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
