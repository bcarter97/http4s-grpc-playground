package io.github.bcarter97.http4sgrpc.server

import cats.Applicative
import cats.effect.std.Console
import cats.syntax.all.*
import fs2.Stream
import io.github.bcarter97.greeter.{Greeter, HelloReply, HelloRequest}
import org.http4s.Headers

final class GreeterServer[F[_] : Applicative : Console] extends Greeter[F] {

  override def sayHello(request: HelloRequest, ctx: Headers): F[HelloReply] =
    response(request)

  override def sayHelloStream(request: HelloRequest, ctx: Headers): Stream[F, HelloReply] =
    Stream.eval(response(request))

  override def sayHelloStreamToStream(request: Stream[F, HelloRequest], ctx: Headers): Stream[F, HelloReply] =
    request.evalMap(response)

  private def response(request: HelloRequest) =
    Console[F]
      .println(s"Received: ${request.name}")
      .as(HelloReply(message = s"Hello, ${request.name}!"))

}
