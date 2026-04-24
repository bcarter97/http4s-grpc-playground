import org.typelevel.scalacoptions.ScalacOptions

val scala3Version = "3.3.7"

Global / onChangedBuildSource := ReloadOnSourceChanges

lazy val root = project
  .in(file("."))
  .enablePlugins(Http4sGrpcPlugin)
  .settings(
    name              := "http4s-grpc-playground",
    version           := "0.1.0-SNAPSHOT",
    scalaVersion      := scala3Version,
    run / fork        := true,
    semanticdbEnabled := true,
    semanticdbVersion := scalafixSemanticdb.revision,
    scalafmtOnCompile := true,
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
  .settings(compiler)

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
