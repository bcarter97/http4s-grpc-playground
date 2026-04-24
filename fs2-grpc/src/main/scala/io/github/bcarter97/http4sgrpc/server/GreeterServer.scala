package io.github.bcarter97.http4sgrpc.server

import cats.Applicative
import cats.syntax.all.*
import fs2.Stream
import io.github.bcarter97.greeter.{GreeterFs2Grpc, HelloReply, HelloRequest}
import io.grpc.Metadata

final class GreeterServer[F[_] : Applicative] extends GreeterFs2Grpc[F, Metadata] {

  override def sayHello(request: HelloRequest, ctx: Metadata): F[HelloReply] =
    response(request).pure

  override def sayHelloStream(request: HelloRequest, ctx: Metadata): Stream[F, HelloReply] =
    Stream(response(request))

  override def sayHelloStreamToStream(request: Stream[F, HelloRequest], ctx: Metadata): Stream[F, HelloReply] =
    request.map(response)

  private def response(request: HelloRequest) =
    HelloReply(message = s"Hello, ${request.name}!")

}
