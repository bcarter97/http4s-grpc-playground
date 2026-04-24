package io.github.bcarter97.http4sgrpc.client

import cats.effect.*
import fs2.Stream
import io.github.bcarter97.greeter.{Greeter, HelloRequest}
import org.http4s.Headers
import org.http4s.ember.client.EmberClientBuilder
import org.http4s.syntax.all.*

import scala.concurrent.duration.*

object Main extends IOApp.Simple {

  override def run: IO[Unit] =
    EmberClientBuilder.default[IO].withHttp2.build.use { client =>
      val greeter = Greeter.fromClient[IO](client, uri"http://localhost:4770")

      for {
        reply <- greeter.sayHello(HelloRequest("World"), Headers.empty)
        _     <- IO.println(s"unaryToUnary: ${reply.message}")

        // Send "Alice", wait for the server to respond, then send "Bob", i.e. next request depends on the previous response
        result <- Deferred[IO, Unit].flatMap { gotFirstReply =>
                    val requests = Stream.emit(HelloRequest("Alice")) ++
                      Stream.eval(gotFirstReply.get.as(HelloRequest("Bob")))

                    greeter
                      .sayHelloStreamToStream(requests, Headers.empty)
                      .head
                      .evalTap(_ => gotFirstReply.complete(()).void)
                      .compile
                      .lastOrError
                      .timeout(5.seconds)
                      .attempt
                  }
        _      <- result match {
                    case Right(r) => IO.println(s"streamToStream: ${r.message}")
                    case Left(_)  => IO.println("streamToStream: timed out waiting for first response")
                  }
      } yield ()
    }

}
