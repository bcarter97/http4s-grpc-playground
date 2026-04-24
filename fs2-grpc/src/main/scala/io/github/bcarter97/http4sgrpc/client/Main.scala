package io.github.bcarter97.http4sgrpc.client

import cats.effect.*
import fs2.Stream
import fs2.grpc.syntax.all.*
import io.github.bcarter97.greeter.{GreeterFs2Grpc, HelloRequest}
import io.grpc.{ManagedChannelBuilder, Metadata}

import scala.concurrent.duration.*

object Main extends IOApp.Simple {

  override def run: IO[Unit] = {
    val channel = ManagedChannelBuilder
      .forAddress("localhost", 4770)
      .usePlaintext()
      .resource[IO]

    channel.use { ch =>
      GreeterFs2Grpc.stubResource[IO](ch).use { greeter =>
        for {
          reply <- greeter.sayHello(HelloRequest("World"), new Metadata)
          _     <- IO.println(s"unaryToUnary: ${reply.message}")

          iShouldNotBeEmittedAndElement1ShouldBeFlushed = Stream.never[IO]

          result <- greeter
                      .sayHelloStreamToStream(
                        Stream.emit(HelloRequest("Alice")) ++ iShouldNotBeEmittedAndElement1ShouldBeFlushed,
                        new Metadata
                      )
                      .head
                      .compile
                      .lastOrError
                      .timeout(5.seconds)
                      .attempt
          _      <- result match {
                      case Right(r) => IO.println(s"streamToStream: ${r.message}")
                      case Left(_)  => IO.println("streamToStream: timed out waiting for first response")
                    }
        } yield ()
      }
    }
  }

}
