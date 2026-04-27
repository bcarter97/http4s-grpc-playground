package io.github.bcarter97.http4sgrpc.server

import cats.effect.*
import cats.syntax.all.*
import com.comcast.ip4s.{host, port}
import fs2.io.net.Network
import io.github.bcarter97.greeter.{Greeter, GreeterProto}
import io.github.bcarter97.http4sgrpc.reflection.ReflectionServer
import io.github.bcarter97.http4sgrpc.server.GreeterServer
import io.grpc.reflection.v1.reflection.ServerReflection
import org.http4s.HttpRoutes
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.server.Server

object Main extends IOApp.Simple {

  override def run: IO[Unit] = {
    val greeterRoutes    = Greeter.toRoutes[IO](GreeterServer[IO])
    val reflectionRoutes = ServerReflection.toRoutes[IO](ReflectionServer[IO](GreeterProto))
    server[IO](greeterRoutes <+> reflectionRoutes).useForever
  }

  private def server[F[_] : Async : Network](routes: HttpRoutes[F]): Resource[F, Server] =
    EmberServerBuilder
      .default[F]
      .withHost(host"localhost")
      .withPort(port"4770")
      .withHttpApp(routes.orNotFound)
      .withHttp2
      .build

}
