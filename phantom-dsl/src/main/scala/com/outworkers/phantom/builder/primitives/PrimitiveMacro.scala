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
package com.outworkers.phantom.builder.primitives

import java.net.InetAddress
import java.nio.ByteBuffer
import java.util.{Date, UUID}

import macrocompat.bundle
import org.joda.time.DateTime

import scala.collection.concurrent.TrieMap
import scala.language.experimental.macros

@bundle
class PrimitiveMacro(val c: scala.reflect.macros.blackbox.Context) {
  import c.universe._

  val rowByNameType = tq"com.datastax.driver.core.GettableByNameData"
  val rowByIndexType = tq"com.datastax.driver.core.GettableByIndexData"
  val pVersion = tq"com.datastax.driver.core.ProtocolVersion"

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
  val tupleValue: Tree = tq"com.datastax.driver.core.TupleValue"
  val localDate: Tree = tq"com.datastax.driver.core.LocalDate"
  val dateTimeType: Tree = tq"org.joda.time.DateTime"
  val localJodaDate: Tree = tq"org.joda.time.LocalDate"
  val bigDecimalType: Tree = tq"scala.math.BigDecimal"
  val inetType: Tree = tq"java.net.InetAddress"
  val bigIntType = tq"scala.math.BigInt"
  val bufferType = tq"java.nio.ByteBuffer"

  val builder = q"com.outworkers.phantom.builder"
  val cql = q"com.outworkers.phantom.builder.query.engine.CQLQuery"
  val syntax = q"com.outworkers.phantom.builder.syntax.CQLSyntax"

  val prefix = q"com.outworkers.phantom.builder.primitives"
  private[this] val versionTerm = q"version"

  def tryT(x: Tree): Tree = tq"scala.util.Try[$x]"
  def tryT(x: Type): Tree = tq"scala.util.Try[$x]"

  def typed[A : c.WeakTypeTag]: Symbol = weakTypeOf[A].typeSymbol

  /**
    * Adds a caching layer for subsequent requests to materialise the same primitive type.
    * This adds a simplistic caching layer that computes primitives based on types.
    */
  val treeCache: TrieMap[Symbol, Tree] = TrieMap.empty[Symbol, Tree]

  def isTuple(tpe: Type): Boolean = {
    tpe.typeSymbol.fullName startsWith "scala.Tuple"
  }

  object Symbols {
    val intSymbol: Symbol = typed[Int]
    val byteSymbol: Symbol = typed[Byte]
    val stringSymbol: Symbol = typed[String]
    val boolSymbol: Symbol = typed[Boolean]
    val shortSymbol: Symbol = typed[Short]
    val longSymbol: Symbol = typed[Long]
    val doubleSymbol: Symbol = typed[Double]
    val floatSymbol: Symbol = typed[Float]
    val dateSymbol: Symbol = typed[Date]
    val listSymbol: Symbol = typed[scala.collection.immutable.List[_]]
    val setSymbol: Symbol = typed[scala.collection.immutable.Set[_]]
    val mapSymbol: Symbol = typed[scala.collection.immutable.Map[_, _]]
    val dateTimeSymbol: Symbol = typed[DateTime]
    val localDateSymbol: Symbol = typed[com.datastax.driver.core.LocalDate]
    val uuidSymbol: Symbol = typed[UUID]
    val jodaLocalDateSymbol: Symbol = typed[org.joda.time.LocalDate]
    val inetSymbol: Symbol = typed[InetAddress]
    val bigInt: Symbol = typed[BigInt]
    val bigDecimal: Symbol = typed[BigDecimal]
    val buffer: Symbol = typed[ByteBuffer]
    val enumValue: Symbol = typed[Enumeration#Value]
    val enum: Symbol = typed[Enumeration]
  }

  def primitive(nm: String): Tree = q"$prefix.Primitives.${TermName(nm)}"

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

  def listPrimitive[T : WeakTypeTag](): Tree = {
    val tpe = weakTypeOf[T]

    val innerTpe = tpe.typeArgs.headOption

    innerTpe match {
      case Some(inner) => {
        q"""$prefix.Primitives.list[$inner]"""
      }
      case None => c.abort(c.enclosingPosition, "Expected inner type to be defined")
    }
  }

  case class TupleType(
    term: TermName,
    cassandraType: Tree,
    extractor: Tree,
    serializer: Tree
  )

  def tupleFields(tpe: Type): List[TupleType] = {
    val sourceTerm = TermName("source")
    tpe.typeArgs.zipWithIndex.map {
      case (argTpe, i) =>
        val currentTerm = TermName(s"tp${i + 1}")
        val tupleRef = TermName("_" + (i + 1).toString)
        val index = q"$i"

        TupleType(
          currentTerm,
          q"$prefix.Primitive[$argTpe].cassandraType",
          fq"$currentTerm <- $prefix.Primitive[$argTpe].fromRow(index = $index, row = $sourceTerm)",
          q"$prefix.Primitive[$argTpe].asCql(tp.$tupleRef)"
        )
    }
  }

  def tuplePrimitive[T : WeakTypeTag](): Tree = {
    val tpe = weakTypeOf[T]

    val comp = tpe.typeSymbol.name.toTermName
    val sourceTerm = TermName("source")

    val fields: List[TupleType] = tupleFields(tpe)

    q"""new $prefix.Primitive[$tpe] {
      override def cassandraType: $strType = {
        $builder.QueryBuilder.Collections
          .tupleType(..${fields.map(_.cassandraType)})
          .queryString
      }

      override def serialize(source: $tpe): $bufferType = ???
      override def deserialize(source: $bufferType): $tpe = ???

      override def fromRow(name: $strType, row: $rowByNameType): ${tryT(tpe)} = {
        if (scala.Option(row).isEmpty || row.isNull(name)) {
          scala.util.Failure(new Exception("Column with name " + name + " is null") with scala.util.control.NoStackTrace)
        } else {
          val $sourceTerm = row.getTupleValue(name)
          for (..${fields.map(_.extractor)}) yield $comp.apply(..${fields.map(_.term)})
        }
      }

      override def asCql(tp: $tpe): $strType = {
        $builder.QueryBuilder.Collections.tupled(..${fields.map(_.serializer)}).queryString
      }

      override def fromString(value: $strType): $tpe = ???

      override def frozen: $boolType = true
    }"""
  }

  def mapPrimitive[T : WeakTypeTag](): Tree = {
    weakTypeOf[T].typeArgs match {
      case k :: v :: Nil => q"""$prefix.Primitives.map[$k, $v]"""
      case _ => c.abort(c.enclosingPosition, "Expected exactly two type arguments to be provided to map")
    }
  }

  def setPrimitive[T : WeakTypeTag](): Tree = {
    val tpe = weakTypeOf[T]

    tpe.typeArgs.headOption match {
      case Some(inner) => q"$prefix.Primitives.set[$inner]"
      case None => c.abort(c.enclosingPosition, "Expected inner type to be defined")
    }
  }

  def enumPrimitive(tpe: Type): Tree = {
    val comp = tpe.typeSymbol.name.toTermName

    q"""
      $prefix.Primitive.derive[$tpe#Value, $strType](_.toString)(
        str =>
          $comp.values.find(_.toString == str) match {
            case Some(value) => value
            case _ => throw new Exception(
              "Value " + str + " not found in enumeration"
            ) with scala.util.control.NoStackTrace
          }
      )
    """
  }

  def enumValuePrimitive(tpe: Type): Tree = {

    val comp = c.parse(s"${tpe.toString.replace("#Value", "").replace(".Value", "")}")

    q"""
      $prefix.Primitive.derive[$tpe, $strType](_.toString)(
        str =>
          $comp.values.find(_.toString == str) match {
            case Some(value) => value
            case _ => throw new Exception(
              "Value " + str + " not found in enumeration"
            ) with scala.util.control.NoStackTrace
          }
      )
    """
  }

  def materializer[T : c.WeakTypeTag]: c.Expr[Primitive[T]] = {
    val wkType = weakTypeOf[T]
    val tpe = wkType.typeSymbol

    val tree = tpe match {
      case _ if isTuple(wkType) => tuplePrimitive[T]()
      case Symbols.boolSymbol => booleanPrimitive
      case Symbols.byteSymbol => bytePrimitive
      case Symbols.shortSymbol => shortPrimitive
      case Symbols.intSymbol => intPrimitive
      case Symbols.longSymbol => longPrimitive
      case Symbols.doubleSymbol => doublePrimitive
      case Symbols.floatSymbol => floatPrimitive
      case Symbols.uuidSymbol => uuidPrimitive
      case Symbols.stringSymbol => stringPrimitive
      case Symbols.dateSymbol => datePrimitive
      case Symbols.dateTimeSymbol => dateTimePrimitive
      case Symbols.localDateSymbol => localDatePrimitive
      case Symbols.jodaLocalDateSymbol => localJodaDatePrimitive
      case Symbols.inetSymbol => inetPrimitive
      case Symbols.bigInt => bigIntPrimitive
      case Symbols.bigDecimal => bigDecimalPrimitive
      case Symbols.buffer => bufferPrimitive
      case Symbols.enum => treeCache.getOrElseUpdate(typed[T], enumPrimitive(wkType))
      case Symbols.enumValue => treeCache.getOrElseUpdate(typed[T], enumValuePrimitive(wkType))
      case Symbols.listSymbol => treeCache.getOrElseUpdate(typed[T], listPrimitive[T]())
      case Symbols.setSymbol => treeCache.getOrElseUpdate(typed[T], setPrimitive[T]())
      case Symbols.mapSymbol => treeCache.getOrElseUpdate(typed[T], mapPrimitive[T]())
      case _ => c.abort(c.enclosingPosition, s"Cannot find primitive implementation for $tpe")
    }

    c.Expr[Primitive[T]](tree)
  }
}
