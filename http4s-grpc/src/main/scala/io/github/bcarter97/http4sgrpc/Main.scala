package io.github.bcarter97.http4sgrpc

import cats.effect.*
import com.comcast.ip4s.{host, port}
import fs2.io.net.Network
import io.github.bcarter97.greeter.Greeter
import io.github.bcarter97.http4sgrpc.greeter.GreeterServer
import org.http4s.HttpRoutes
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.syntax.all.*

object Main extends IOApp.Simple {

  override def run: IO[Unit] = {
    val routes = Greeter.toRoutes(GreeterServer[IO])
    server[IO](routes).useForever
  }

  private def server[F[_] : Async : Network](routes: HttpRoutes[F]) =
    EmberServerBuilder
      .default[F]
      .withHost(host"localhost")
      .withPort(port"4770")
      .withHttpApp(routes.orNotFound)
      .withHttp2
      .build

}
