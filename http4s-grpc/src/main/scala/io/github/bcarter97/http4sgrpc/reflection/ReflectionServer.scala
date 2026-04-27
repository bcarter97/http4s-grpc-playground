package io.github.bcarter97.http4sgrpc.reflection

import fs2.Stream
import io.grpc.reflection.v1.reflection.*
import io.grpc.reflection.v1.reflection.ServerReflectionRequest.MessageRequest
import io.grpc.reflection.v1.reflection.ServerReflectionResponse.MessageResponse
import org.http4s.Headers
import scalapb.GeneratedFileObject

/** @see
  *   https://github.com/grpc/grpc-java/blob/master/services/src/main/java/io/grpc/protobuf/services/ProtoReflectionServiceV1.java
  */
final class ReflectionServer[F[_]] private (index: ReflectionIndex) extends ServerReflection[F] {
  import ReflectionServer.GrpcStatus

  override def serverReflectionInfo(
      request: Stream[F, ServerReflectionRequest],
      ctx: Headers
  ): Stream[F, ServerReflectionResponse] =
    request.map(handleRequest)

  private def handleRequest(req: ServerReflectionRequest): ServerReflectionResponse = {
    val response = req.messageRequest match {
      case MessageRequest.ListServices(_) =>
        MessageResponse.ListServicesResponse(
          ListServiceResponse(index.serviceNames.map(ServiceResponse(_)))
        )

      case MessageRequest.FileByFilename(filename) =>
        index.fileDescriptorsByFilename(filename) match {
          case Some(fds) => MessageResponse.FileDescriptorResponse(FileDescriptorResponse(fds))
          case None      => errorResponse(GrpcStatus.NOT_FOUND, "File not found.")
        }

      case MessageRequest.FileContainingSymbol(symbol) =>
        index.fileDescriptorsBySymbol(symbol) match {
          case Some(fds) => MessageResponse.FileDescriptorResponse(FileDescriptorResponse(fds))
          case None      => errorResponse(GrpcStatus.NOT_FOUND, "Symbol not found.")
        }

      case MessageRequest.FileContainingExtension(_) =>
        errorResponse(GrpcStatus.NOT_FOUND, "Extension not found.")

      case MessageRequest.AllExtensionNumbersOfType(_) =>
        errorResponse(GrpcStatus.NOT_FOUND, "Type not found.")

      case MessageRequest.Empty =>
        errorResponse(GrpcStatus.UNIMPLEMENTED, s"not implemented ${req.messageRequest.getClass.getSimpleName}")
    }
    ServerReflectionResponse(
      validHost = req.host,
      originalRequest = Some(req),
      messageResponse = response
    )
  }

  private def errorResponse(code: Int, message: String): MessageResponse =
    MessageResponse.ErrorResponse(ErrorResponse(errorCode = code, errorMessage = message))
}

object ReflectionServer {

  def apply[F[_]](files: GeneratedFileObject*): ReflectionServer[F] = {
    val index = ReflectionIndex.fromFiles(files.toSeq)
    new ReflectionServer[F](index)
  }

  private object GrpcStatus {
    val NOT_FOUND: Int     = 5
    val UNIMPLEMENTED: Int = 12
  }
}
