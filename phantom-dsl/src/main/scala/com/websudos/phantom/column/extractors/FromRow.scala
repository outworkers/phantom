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
package com.websudos.phantom.column.extractors

import com.datastax.driver.core.Row
import com.websudos.phantom.Manager
import com.websudos.phantom.builder.primitives.Primitive
import shapeless.{HList, HNil, Generic, :: => #:}
import scala.util.{Failure, Success, Try}

trait FromRow[L <: HList] { def apply(row: List[(String, Row)]): Try[L] }

object FromRow {

  def apply[L <: HList](implicit fromRow: FromRow[L]): FromRow[L] = fromRow

  def fromFunc[L <: HList](f: List[(String, Row)] => Try[L]): FromRow[L] = new FromRow[L] {
    def apply(row: List[(String, Row)]) = f(row)
  }

  implicit val hnilExtractor: FromRow[HNil] = fromFunc {
    case Nil => Success(HNil)
    case _ => Failure(new RuntimeException("No more rows expected"))
  }

  implicit def hconsExtractor[H : Primitive, T <: HList : FromRow]: FromRow[H #: T] =
    fromFunc {
      case (field, row) :: t => for {
        hv <- Primitive[H].fromRow(field, row)
        tv <- FromRow[T].apply(t)
      } yield hv +: tv
      case Nil => Failure(new RuntimeException("Expected more cells"))
    }


  trait RowParser[A] {

    def apply[L <: HList](row: Row)(implicit
      tag: scala.reflect.runtime.universe.TypeTag[A],
      gen: Generic.Aux[A, L],
      fromRow: FromRow[L]
    ): A = {
      val applier = new RowParser[A] {}
      val accessors = Helper.classAccessors[A]

      applier.apply(accessors zip List.tabulate(accessors.size)(_ => row))  match {
        case Success(value) => value
        case Failure(ex) =>
          Manager.logger.error(s"Unable to parse value for from row", ex)
          throw ex
      }
    }

    def apply[L <: HList](row: List[(String, Row)])(implicit
      gen: Generic.Aux[A, L],
      fromRow: FromRow[L]
    ): Try[A] = fromRow(row).map(gen.from)
  }
}
