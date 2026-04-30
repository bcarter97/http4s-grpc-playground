package io.github.bcarter97.http4sgrpc.server

import cats.Monad
import cats.data.NonEmptyList
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
import scalapb.GeneratedFileObject

object Main extends IOApp.Simple {

  override def run: IO[Unit] = {
    val allRoutes = services.routes <+> reflectionRoutes(services.protos)
    server[IO](allRoutes).useForever
  }

  final case class GrpcServices[F[_] : Monad](services: NonEmptyList[GrpcService[F]]) {
    val routes: HttpRoutes[F]                     = services.map(_.routes).reduceK
    val protos: NonEmptyList[GeneratedFileObject] = services.map(_.proto)
  }

  object GrpcServices {
    def of[F[_] : Monad](head: GrpcService[F], tail: GrpcService[F]*): GrpcServices[F] =
      GrpcServices(NonEmptyList(head, tail.toList))
  }

  final case class GrpcService[F[_]](routes: HttpRoutes[F], proto: GeneratedFileObject)

  private def services: GrpcServices[IO] =
    GrpcServices.of(
      GrpcService[IO](Greeter.toRoutes[IO](GreeterServer[IO]), GreeterProto)
    )

  private def reflectionRoutes(files: NonEmptyList[GeneratedFileObject]): HttpRoutes[IO] =
    ServerReflection.toRoutes[IO](ReflectionServer[IO](files.toList*))

  private def server[F[_] : Async : Network](routes: HttpRoutes[F]): Resource[F, Server] =
    EmberServerBuilder
      .default[F]
      .withHost(host"localhost")
      .withPort(port"4770")
      .withHttpApp(routes.orNotFound)
      .withHttp2
      .build

}
