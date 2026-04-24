package io.github.bcarter97.http4sgrpc.server

import cats.Applicative
import cats.syntax.all.*
import fs2.Stream
import io.github.bcarter97.greeter.{Greeter, HelloReply, HelloRequest}
import org.http4s.Headers

final class GreeterServer[F[_] : Applicative] extends Greeter[F] {

  override def sayHello(request: HelloRequest, ctx: Headers): F[HelloReply] =
    response(request).pure

  override def sayHelloStream(request: HelloRequest, ctx: Headers): Stream[F, HelloReply] =
    Stream(response(request))

  override def sayHelloStreamToStream(request: Stream[F, HelloRequest], ctx: Headers): Stream[F, HelloReply] =
    request.map(response)

  private def response(request: HelloRequest) =
    HelloReply(message = s"Hello, ${request.name}!")

}
