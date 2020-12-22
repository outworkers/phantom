/*
 * Copyright 2013 - 2020 Outworkers Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.outworkers.phantom.builder.primitives

import java.nio.{BufferUnderflowException, ByteBuffer}

import com.datastax.driver.core._
import com.datastax.driver.core.exceptions.{DriverInternalError, InvalidTypeException}
import com.outworkers.phantom.builder.QueryBuilder

import scala.collection.compat._

object Utils {
  private[phantom] def unsupported(version: ProtocolVersion): DriverInternalError = {
    new DriverInternalError(s"Unsupported protocol version $version")
  }

  private[this] val baseColSize = 4

  private[this] def sizeOfValue(value: ByteBuffer, version: ProtocolVersion): Int = {
    version match {
      case ProtocolVersion.V1 | ProtocolVersion.V2 =>
        val elemSize = value.remaining

        if (elemSize > 65535) {
          throw new IllegalArgumentException(
            s"Native protocol version $version supports only elements " +
              s"with size up to 65535 bytes - but element size is $elemSize bytes"
          )
        }

        2 + elemSize
      case ProtocolVersion.V3 | ProtocolVersion.V4 | ProtocolVersion.V5 =>
        if (value == Primitive.nullValue) baseColSize else baseColSize + value.remaining

      case _ => throw unsupported(version)
    }
  }

  private[this] def sizeOfCollectionSize(version: ProtocolVersion): Int = version match {
    case ProtocolVersion.V1 | ProtocolVersion.V2 => 2
    case ProtocolVersion.V3 | ProtocolVersion.V4 | ProtocolVersion.V5 => baseColSize
    case _ => throw unsupported(version)
  }

  /**
    * Utility method that "packs" together a list of {@link ByteBuffer}s containing
    * serialized collection elements.
    * Mainly intended for use with collection codecs when serializing collections.
    *
    * @param buffers  the collection elements
    * @param elements the total number of elements
    * @param version  the protocol version to use
    * @return The serialized collection
    */
  def pack(
    buffers: Array[ByteBuffer],
    elements: Int,
    version: ProtocolVersion
  ): ByteBuffer = {
    val size = buffers.foldLeft(0)((acc, b) => acc + sizeOfValue(b, version))

    val result = ByteBuffer.allocate(sizeOfCollectionSize(version) + size)

    CodecUtils.writeSize(result, elements, version)

    for (bb <- buffers) CodecUtils.writeValue(result, bb, version)
    result.flip.asInstanceOf[ByteBuffer]
  }

  /**
    * Utility method that "packs" together a list of {{java.nio.ByteBuffer}}s containing
    * serialized collection elements.
    * Mainly intended for use with collection codecs when serializing collections.
    *
    * @param buffers  the collection elements
    * @param elements the total number of elements
    * @param version  the protocol version to use
    * @return The serialized collection
    */
  def pack[M[X] <: Iterable[X]](
    buffers: M[ByteBuffer],
    elements: Int,
    version: ProtocolVersion
  ): ByteBuffer = {
    val size = buffers.foldLeft(0)((acc, b) => acc + sizeOfValue(b, version))

    val result = ByteBuffer.allocate(sizeOfCollectionSize(version) + size)

    CodecUtils.writeSize(result, elements, version)

    for (bb <- buffers) CodecUtils.writeValue(result, bb, version)
    result.flip.asInstanceOf[ByteBuffer]
  }
}

object Primitives {

  private[phantom] def emptyCollection: ByteBuffer = ByteBuffer.allocate(0)

  private[this] def collectionPrimitive[M[X] <: IterableOnce[X], RR](
    cType: String,
    converter: M[RR] => String
  )(
    implicit ev: Primitive[RR],
    cbf: Factory[RR, M[RR]]
  ): Primitive[M[RR]] = new Primitive[M[RR]] {
    override def frozen: Boolean = true

    override def shouldFreeze: Boolean = true

    override def asCql(value: M[RR]): String = converter(value)

    override val dataType: String = cType

    override def serialize(coll: M[RR], version: ProtocolVersion): ByteBuffer = {
      coll match {
        case Primitive.nullValue => Primitive.nullValue
        case c if c.iterator.isEmpty => Utils.pack(new Array[ByteBuffer](coll.size), coll.size, version)
        case _ =>
          val bbs = coll.iterator.foldLeft(Seq.empty[ByteBuffer]) { (acc, elt) =>
            notNull(elt, "Collection elements cannot be null")
            acc :+ ev.serialize(elt, version)
          }

          Utils.pack(bbs, coll.iterator.size, version)
      }
    }

    override def deserialize(bytes: ByteBuffer, version: ProtocolVersion): M[RR] = {
      if (bytes == Primitive.nullValue || bytes.remaining() == 0) {
        cbf.newBuilder.result()
      } else {
        try {
          val input = bytes.duplicate()
          val size = CodecUtils.readSize(input, version)
          val coll = cbf.newBuilder
          coll.sizeHint(size)

          for (_ <- 0 until size) {
            val databb = CodecUtils.readValue(input, version)
            coll += ev.deserialize(databb, version)
          }
          coll.result()
        } catch {
          case e: BufferUnderflowException =>
            throw new InvalidTypeException("Not enough bytes to deserialize collection", e)
        }
      }
    }
  }

  def list[T](implicit ev: Primitive[T]): Primitive[List[T]] = {
    collectionPrimitive[List, T](
      QueryBuilder.Collections.listType(ev.cassandraType).queryString,
      value => QueryBuilder.Collections.serialize(value.map(ev.asCql)).queryString
    )
  }

  def set[T](implicit ev: Primitive[T]): Primitive[Set[T]] = {
    collectionPrimitive[Set, T](
      QueryBuilder.Collections.setType(ev.cassandraType).queryString,
      value => QueryBuilder.Collections.serialize(value.map(ev.asCql)).queryString
    )
  }


  def option[T : Primitive]: Primitive[Option[T]] = {
    val ev = implicitly[Primitive[T]]

    val nullString = "null"

    new Primitive[Option[T]] {

      def serialize(obj: Option[T], protocol: ProtocolVersion): ByteBuffer = {
        obj.fold(
          Primitive.nullValue.asInstanceOf[ByteBuffer]
        )(ev.serialize(_, protocol))
      }

      def deserialize(source: ByteBuffer, protocol: ProtocolVersion): Option[T] = {
        if (source == Primitive.nullValue) {
          None
        } else {
          Some(ev.deserialize(source, protocol))
        }
      }

      override def dataType: String = ev.dataType

      override def asCql(value: Option[T]): String = {
        value.map(ev.asCql).getOrElse(nullString)
      }
    }
  }

}
