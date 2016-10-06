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

import com.datastax.driver.core.utils.Bytes
import com.datastax.driver.core.{GettableData, LocalDate, Row}
import com.websudos.phantom.builder.query.CQLQuery
import com.websudos.phantom.builder.syntax.CQLSyntax
import org.joda.time.{DateTime, DateTimeZone}

import scala.util.control.NoStackTrace
import scala.util.{Failure, Try}

private[phantom] object DateSerializer {

  def asCql(date: Date): String = date.getTime.toString

  def asCql(date: LocalDate): String = date.getMillisSinceEpoch.toString

  def asCql(date: org.joda.time.LocalDate): String = date.toString

  def asCql(date: DateTime): String = date.getMillis.toString
}

abstract class Primitive[RR] {

  type PrimitiveType

  protected[this] def nullCheck[T](column: String, row: GettableData)(fn: GettableData => T): Try[T] = {
    if (Option(row).isEmpty || row.isNull(column)) {
      Failure(new Exception(s"Column $column is null") with NoStackTrace)
    } else {
      Try(fn(row))
    }
  }

  def asCql(value: RR): String

  def cassandraType: String

  def fromRow(column: String, row: GettableData): Try[RR]

  def fromString(value: String): RR

  def clz: Class[PrimitiveType]

  def extract(obj: PrimitiveType): RR = identity(obj).asInstanceOf[RR]

}

object Primitive {

  implicit def materializer[T]: Primitive[T] = macro PrimitiveMacro.materializer[T]

  def apply[RR : Primitive]: Primitive[RR] = implicitly[Primitive[RR]]
}
