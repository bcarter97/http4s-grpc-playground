package io.github.bcarter97.http4sgrpc

import cats.effect.*
import cats.effect.syntax.all.*
import fs2.grpc.syntax.all.*
import io.github.bcarter97.greeter.GreeterFs2Grpc
import io.github.bcarter97.http4sgrpc.greeter.GreeterServer
import io.grpc.netty.shaded.io.grpc.netty.NettyServerBuilder
import io.grpc.protobuf.services.ProtoReflectionService
import io.grpc.{Server, ServerServiceDefinition}

import java.net.InetSocketAddress
import scala.jdk.CollectionConverters.*

object Main extends IOApp.Simple {

  override def run: IO[Unit] = {
    val serviceResource = GreeterFs2Grpc.bindServiceResource(GreeterServer[IO])
    serviceResource
      .use(service => server[IO](service).useForever)
  }

  private def server[F[_] : Async](services: ServerServiceDefinition*): Resource[F, Server] =
    for {
      reflectionService <- Async[F].delay(ProtoReflectionService.newInstance().bindService()).toResource
      server            <- NettyServerBuilder
                             .forAddress(InetSocketAddress("localhost", 4770))
                             .addServices((reflectionService +: services).asJava)
                             .resource[F]
                             .evalMap(server => Async[F].delay(server.start()))
    } yield server
}
