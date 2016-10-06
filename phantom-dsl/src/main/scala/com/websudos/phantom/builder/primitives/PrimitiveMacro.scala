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
import java.util.{Date, UUID}

import macrocompat.bundle
import org.joda.time.DateTime

import scala.language.experimental.macros

@bundle
class PrimitiveMacro(val c: scala.reflect.macros.blackbox.Context) {
  import c.universe._

  val rowType = tq"com.datastax.driver.core.Row"
  val boolType = tq"scala.Boolean"
  val strType: Tree = tq"java.lang.String"
  val intType: Tree = tq"scala.Int"
  val byteType: Tree = tq"scala.Byte"
  val doubleType: Tree = tq"scala.Double"
  val shortType: Tree = tq"scala.Short"
  val uuidType: Tree = tq"java.util.UUID"
  val longType: Tree = tq"scala.Long"
  val floatType: Tree = tq"scala.Float"
  val dateType: Tree = tq"java.util.Date"
  val localDate: Tree = tq"com.datastax.driver.core.LocalDate"
  val dateTimeType: Tree = tq"org.joda.time.DateTime"
  val localJodaDate: Tree = tq"org.joda.time.LocalDate"
  val bigDecimalType: Tree = tq"scala.math.BigDecimal"
  val inetType: Tree = tq"java.net.InetAddress"
  val bigIntType = tq"scala.math.BigInt"
  val bufferType = tq"java.nio.ByteBuffer"

  val cql = q"com.websudos.phantom.builder.query.CQLQuery"
  val syntax = q"com.websudos.phantom.builder.syntax.CQLSyntax"

  def tryT(x: Tree): Tree = tq"scala.util.Try[$x]"

  def typed[A : c.WeakTypeTag] = weakTypeOf[A].typeSymbol

  def materializer[T : c.WeakTypeTag]: c.Expr[Primitive[T]] = {
    val tpe = weakTypeOf[T].typeSymbol

    val primitiveTree: Tree = if (tpe == typed[String]) {
      stringPrimitive
    } else if (tpe == typed[Int]) {
      intPrimitive
    } else if (tpe == typed[Byte]) {
      bytePrimitive
    } else if (tpe == typed[Boolean]) {
      booleanPrimitive
    } else if (tpe == typed[Short]) {
      shortPrimitive
    } else if (tpe == typed[Long]) {
      longPrimitive
    } else if (tpe == typed[Double]) {
      doublePrimitive
    } else if (tpe == typed[Float]) {
      floatPrimitive
    } else if (tpe == typed[UUID]) {
      uuidPrimitive
    } else if (tpe == typed[Date]) {
      datePrimitive
    } else if (tpe == typed[DateTime]) {
      dateTimePrimitive
    } else if (tpe == typed[com.datastax.driver.core.LocalDate]) {
      localDatePrimitive
    } else if (tpe == typed[org.joda.time.LocalDate]) {
      localJodaDatePrimitive
    } else if (tpe == typed[InetAddress]) {
      inetPrimitive
    } else if (tpe == typed[BigDecimal]) {
      bigDecimalPrimitive
    } else if (tpe == typed[BigInt]) {
      bigIntPrimitive
    } else if (tpe == typed[ByteBuffer]) {
      bufferPrimitive
    } else if (tpe == typed[Enumeration#Value]) {
      enumPrimitive[T]()
    } else {
      c.abort(c.enclosingPosition, s"Cannot find primitive implemention for $tpe")
    }

    c.Expr[Primitive[T]](primitiveTree)
  }

  def primitive(nm: String): Tree = c.parse(s"new com.websudos.phantom.builder.primitives.Primitives.$nm")

  val booleanPrimitive: Tree = primitive("BooleanIsPrimitive")

  val stringPrimitive: Tree = primitive("StringPrimitive")

  val intPrimitive: Tree = primitive("IntPrimitive")

  val shortPrimitive: Tree = primitive("SmallIntPrimitive")

  val bytePrimitive: Tree = primitive("TinyIntPrimitive")

  val doublePrimitive: Tree = primitive("DoublePrimitive")

  val longPrimitive: Tree = primitive("LongPrimitive")

  val floatPrimitive: Tree = primitive("FloatPrimitive")

  val uuidPrimitive: Tree = primitive("UUIDPrimitive")

  val datePrimitive: Tree = primitive("DateIsPrimitive")

  val localDatePrimitive: Tree = primitive("LocalDateIsPrimitive")

  val dateTimePrimitive: Tree = primitive("DateTimeIsPrimitive")

  val localJodaDatePrimitive: Tree = primitive("JodaLocalDateIsPrimitive")

  val bigDecimalPrimitive: Tree = primitive("BigDecimalIsPrimitive")

  val inetPrimitive: Tree = primitive("InetAddressPrimitive")

  val bigIntPrimitive: Tree = primitive("BigIntPrimitive")

  val bufferPrimitive: Tree = primitive("BlobIsPrimitive")

  def enumPrimitive[T]()(implicit tag: WeakTypeTag[T]): Tree = {
    val tpe = tag.tpe
    val comp = c.parse(s"${tag.tpe.toString.replace("#Value", "")}")

    q""" new com.websudos.phantom.builder.primitives.Primitive[$tpe] {
      val strP = implicitly[com.websudos.phantom.builder.primitives.Primitive[String]]

      override type PrimitiveType = java.lang.String

      override def cassandraType: $strType = strP.cassandraType

      override def fromRow(name: $strType, row: $rowType): scala.util.Try[$tpe] = {
        nullCheck(name, row) {
          r => $comp.values.find(_.toString == r.getString(name)) match {
            case Some(value) => value
            case _ => throw new Exception("Value not found in enumeration") with scala.util.control.NoStackTrace
          }
        }
      }

      override def asCql(value: $tpe): String = {
        strP.asCql(value.toString)
      }

      override def fromString(value: $strType): $tpe = {
        $comp.values.find(value == _.toString).getOrElse(None.orNull)
      }

      override def clz: Class[$strType] = classOf[$strType]
    }"""
  }
}
