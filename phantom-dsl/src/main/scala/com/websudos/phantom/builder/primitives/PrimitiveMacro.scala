/*
 * Copyright 2013-2015 Websudos, Limited.
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * - Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 *
 * - Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *
 * - Explicit consent must be obtained from the copyright owner, Outworkers Limited before any redistribution is made.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package com.websudos.phantom.builder.primitives

import java.net.InetAddress
import java.nio.ByteBuffer
import java.util.UUID

import macrocompat.bundle

import scala.language.experimental.macros
import org.joda.time.{DateTime, LocalDate}

@bundle
class PrimitiveMacro(val c: scala.reflect.macros.blackbox.Context) {
  import c.universe._

  val cache = new CachedPrimitiveTrees(c)

  class TypeHolder[A : TypeTag] {
    def unapply: Option[Symbol] = Some(typeOf[A].typeSymbol)
  }

  def materializer[T : c.WeakTypeTag]: c.Expr[Primitive[_]] = {
    val tpe = weakTypeOf[T].typeSymbol

    val tree = tpe match {
      case x: TypeHolder[String] => cache.stringPrimitive
      case x: TypeHolder[Byte] => cache.bytePrimitive
      case x: TypeHolder[Boolean] => cache.booleanPrimitive
      case x: TypeHolder[Short] => cache.shortPrimitive
      case x: TypeHolder[Long] => cache.longPrimitive
      case x: TypeHolder[Double] => cache.doublePrimitive
      case x: TypeHolder[Float] => cache.floatPrimitive
      case x: TypeHolder[UUID] => cache.uuidPrimitive
      case x: TypeHolder[DateTime] => cache.dateTimePrimitive
      case x: TypeHolder[LocalDate] => cache.localJodaDate
      case x: TypeHolder[InetAddress] => cache.inetType
      case x: TypeHolder[BigDecimal] => cache.bigDecimalType
      case x: TypeHolder[BigInt] => cache.bigIntType
      case x: TypeHolder[ByteBuffer] => cache.bufferPrimitive
      case x: TypeHolder[_ <: Enumeration] => enumMaterializer[T]
      case _ => c.abort(c.enclosingPosition, s"Cannot find primitive implemention for $tpe")
    }

    c.Expr[Primitive[_]](tree)
  }

  def enumMaterializer[T <: Enumeration : c.WeakTypeTag]: c.Expr[Primitive[_]] = {
    val tpe = weakTypeOf[T]
    val companion = tpe.typeSymbol.companion

    val tree = q"""
    new com.websudos.phantom.builder.primitives.Primitive[$tpe#Value] {

      override type PrimitiveType = java.lang.String

      override def cassandraType: String = com.websudos.phantom.builder.primitives.Primitive[String].cassandraType

      override def fromRow(name: String, row: com.datastax.driver.core.Row): scala.util.Try[$tpe#Value] = {
        nullCheck(name, row) {
          r => $companion.values.find(_.toString == r.getString(name)) match {
            case Some(value) => value
            case _ => throw new Exception("Value " + name + " not found in enumeration") with scala.util.control.NoStackTrace
          }
        }
      }

      override def asCql(value: $tpe#Value): String = {
        com.websudos.phantom.builder.primitives.Primitive[String].asCql(value.toString)
      }

      override def fromString(value: String): $tpe#Value = {
        $companion.values.find(value == _.toString).getOrElse(None.orNull)
      }

      override def clz: Class[String] = classOf[java.lang.String]
    }"""

    c.Expr[Primitive[T#Value]](tree)
  }
}
