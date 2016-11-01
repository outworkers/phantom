/*
 * Copyright 2013 - 2017 Outworkers Ltd.
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
package com.outworkers.phantom.column.extractors

import com.datastax.driver.core.Row
import com.outworkers.phantom.Manager
import com.outworkers.phantom.builder.primitives.Primitive
import shapeless.{Generic, HList, HNil, :: => #:}

import scala.util.{Failure, Success, Try}
import scala.reflect.runtime.universe._

trait FromRow[L <: HList] { def apply(row: List[(String, Row)]): Try[L] }

object FromRow {

  def classAccessors[T : TypeTag]: List[String] = {
    typeOf[T].members.collect {
      case m: MethodSymbol if m.isCaseAccessor => m.name.decodedName.toString
    }.toList.reverse
  }

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
      case Nil => Failure(new RuntimeException("Expected more columns to extract"))
    }


  trait RowParser[A] {

    def apply[L <: HList](row: Row)(implicit
      tag: scala.reflect.runtime.universe.TypeTag[A],
      gen: Generic.Aux[A, L],
      fromRow: FromRow[L]
    ): A = {
      val applier = new RowParser[A] {}
      val accessors = classAccessors[A]

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
