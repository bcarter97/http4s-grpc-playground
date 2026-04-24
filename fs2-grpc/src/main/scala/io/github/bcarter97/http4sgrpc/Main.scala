package io.github.bcarter97.http4sgrpc

import cats.effect.*
import fs2.grpc.syntax.all.*
import io.github.bcarter97.greeter.GreeterFs2Grpc
import io.github.bcarter97.http4sgrpc.greeter.GreeterServer
import io.grpc.ServerServiceDefinition
import io.grpc.netty.shaded.io.grpc.netty.NettyServerBuilder

import java.net.InetSocketAddress
import scala.jdk.CollectionConverters.*

object Main extends IOApp.Simple {

  override def run: IO[Unit] = {
    val serviceResource = GreeterFs2Grpc.bindServiceResource(GreeterServer[IO])
    serviceResource
      .use(service => server[IO](service).useForever)
  }

  private def server[F[_] : Async](services: ServerServiceDefinition*) =
    NettyServerBuilder
      .forAddress(InetSocketAddress("localhost", 4770))
      .addServices(services.asJava)
      .resource[F]
      .evalMap(server => Async[F].delay(server.start()))
}
