package io.github.bcarter97.http4sgrpc.reflection

import cats.syntax.all.*
import com.google.protobuf.ByteString
import scalapb.GeneratedFileObject
import scalapb.descriptors.{Descriptor, FileDescriptor}

/** Equivalent of the Java ServerReflectionIndex
  *
  * @see
  *   https://github.com/grpc/grpc-java/blob/b7e01a6e88d13ec2c0634cfcb9924ddc0d860ba1/services/src/main/java/io/grpc/protobuf/services/ProtoReflectionServiceV1.java#L330
  */
final class ReflectionIndex private (
    val serviceNames: List[String],
    byFilename: Map[String, Seq[ByteString]],
    bySymbol: Map[String, Seq[ByteString]]
) {

  def fileDescriptorsByFilename(filename: String): Option[Seq[ByteString]] =
    byFilename.get(filename)

  def fileDescriptorsBySymbol(symbol: String): Option[Seq[ByteString]] =
    bySymbol.get(symbol)
}

object ReflectionIndex {

  def fromFiles(files: Seq[GeneratedFileObject]): ReflectionIndex = {
    val allFiles = collectAllFiles(files)

    val fileBytes: Map[String, ByteString] = allFiles
      .map(file => file.scalaDescriptor.fullName -> ByteString.copyFrom(file.javaDescriptor.toProto.toByteArray))
      .toMap

    val deps: Map[String, List[String]] = allFiles
      .map(file => file.scalaDescriptor.fullName -> file.dependencies.map(_.scalaDescriptor.fullName).toList)
      .toMap

    val byFilename: Map[String, Seq[ByteString]] =
      fileBytes.keys.map(name => name -> transitiveDeps(name, fileBytes, deps)).toMap

    val bySymbol: Map[String, Seq[ByteString]] = allFiles
      .flatMap(file => symbolNames(file.scalaDescriptor).tupleRight(file.scalaDescriptor.fullName))
      .toMap
      .flatMap((symbol, filename) => byFilename.get(filename).tupleLeft(symbol))

    val serviceNames = allFiles.flatMap(_.scalaDescriptor.services.map(_.fullName))

    new ReflectionIndex(serviceNames, byFilename, bySymbol)
  }

  private def collectAllFiles(files: Seq[GeneratedFileObject]): List[GeneratedFileObject] = {
    @annotation.tailrec
    def loop(
        queue: List[GeneratedFileObject],
        visited: Set[String],
        acc: List[GeneratedFileObject]
    ): List[GeneratedFileObject] =
      queue match {
        case Nil          => acc.reverse
        case file :: rest =>
          val name = file.scalaDescriptor.fullName
          if (visited.contains(name)) loop(rest, visited, acc)
          else loop(file.dependencies.toList ::: rest, visited + name, file :: acc)
      }
    loop(files.toList, Set.empty, Nil)
  }

  private def transitiveDeps(
      root: String,
      fileBytes: Map[String, ByteString],
      deps: Map[String, List[String]]
  ): Seq[ByteString] = {
    @annotation.tailrec
    def loop(queue: List[String], visited: Set[String], acc: List[ByteString]): List[ByteString] =
      queue match {
        case Nil          => acc.reverse
        case name :: rest =>
          if (visited.contains(name)) loop(rest, visited, acc)
          else
            fileBytes.get(name) match {
              case Some(bytes) => loop(deps.getOrElse(name, Nil) ::: rest, visited + name, bytes :: acc)
              case None        => loop(rest, visited + name, acc)
            }
      }
    loop(List(root), Set.empty, Nil)
  }

  private def symbolNames(descriptor: FileDescriptor): Seq[String] = {
    val services = descriptor.services.flatMap(service => service.fullName +: service.methods.map(_.fullName))
    val messages = descriptor.messages.flatMap(messageSymbols)
    val enums    = descriptor.enums.map(_.fullName)
    services ++ messages ++ enums
  }

  private def messageSymbols(message: Descriptor): Seq[String] =
    message.fullName +: (message.nestedMessages.flatMap(messageSymbols) ++ message.enums.map(_.fullName))
}
