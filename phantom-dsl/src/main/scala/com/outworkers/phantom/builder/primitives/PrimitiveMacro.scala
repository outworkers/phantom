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
import java.nio.{BufferUnderflowException, ByteBuffer}
import java.util.{Date, UUID}

import com.datastax.driver.core.exceptions.InvalidTypeException
import com.outworkers.phantom.builder.query.prepared.ListValue
import com.outworkers.phantom.macros.toolbelt.BlackboxToolbelt
import org.joda.time.DateTime

import scala.collection.concurrent.TrieMap
import scala.reflect.macros.blackbox

@macrocompat.bundle
class PrimitiveMacro(override val c: blackbox.Context) extends BlackboxToolbelt {
  import c.universe._

  def printType(tpe: Type): String = showCode(tq"${tpe.dealias}")

  val rowByNameType = tq"_root_.com.datastax.driver.core.GettableByNameData"
  val rowByIndexType = tq"_root_.com.outworkers.phantom.IndexedRow"
  val protocolVersion = tq"_root_.com.datastax.driver.core.ProtocolVersion"
  private[this] val versionTerm = q"version"

  val boolType = tq"_root_.scala.Boolean"
  val strType: Tree = tq"_root_.java.lang.String"
  val intType: Tree = tq"_root_.scala.Int"
  val byteType: Tree = tq"_root_.scala.Byte"
  val doubleType: Tree = tq"_root_.scala.Double"
  val shortType: Tree = tq"_root_.scala.Short"
  val uuidType: Tree = tq"_root_.java.util.UUID"
  val longType: Tree = tq"_root_.scala.Long"
  val floatType: Tree = tq"_root_.scala.Float"
  val dateType: Tree = tq"_root_.java.util.Date"
  val tupleValue: Tree = tq"_root_.com.datastax.driver.core.TupleValue"
  val localDate: Tree = tq"_root_.com.datastax.driver.core.LocalDate"
  val dateTimeType: Tree = tq"_root_.org.joda.time.DateTime"
  val localJodaDate: Tree = tq"_root_.org.joda.time.LocalDate"
  val bigDecimalType: Tree = tq"_root_.scala.math.BigDecimal"
  val inetType: Tree = tq"_root_.java.net.InetAddress"
  val bigIntType = tq"_root_.scala.math.BigInt"
  val bufferType = tq"_root_.java.nio.ByteBuffer"
  val bufferCompanion = q"_root_.java.nio.ByteBuffer"

  val listValueType = typeOf[ListValue[_]]
  val bufferException = typeOf[BufferUnderflowException]
  val invalidTypeException = typeOf[InvalidTypeException]

  val codecUtils = q"_root_.com.datastax.driver.core.CodecUtils"
  val builder = q"_root_.com.outworkers.phantom.builder"
  val cql = q"_root_.com.outworkers.phantom.builder.query.engine.CQLQuery"
  val syntax = q"_root_.com.outworkers.phantom.builder.syntax.CQLSyntax"

  val prefix = q"_root_.com.outworkers.phantom.builder.primitives"

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

  def isOption(tpe: Type): Boolean = {
    tpe.typeSymbol.fullName startsWith "scala.Option"
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

  def listPrimitive(tpe: Type): Tree = {
    val innerTpe = tpe.typeArgs.headOption

    innerTpe match {
      case Some(inner) => q"""$prefix.Primitives.list[$inner]"""
      case None => c.abort(c.enclosingPosition, "Expected inner type to be defined")
    }
  }

  case class TupleType(
    term: TermName,
    cassandraType: Tree,
    extractor: Tree,
    serializer: Tree,
    tpe: Type
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
          q"$prefix.Primitive[$argTpe].dataType",
          fq"$currentTerm <- $prefix.Primitive[$argTpe].fromRow(index = $index, row = $sourceTerm)",
          q"$prefix.Primitive[$argTpe].asCql(tp.$tupleRef)",
          argTpe
        )
    }
  }

  def tupleTerm(index: Int, aug: Int = 1): TermName = {
    TermName("_" + (index + aug).toString)
  }

  val sourceTerm = TermName("source")
  def fieldTerm(i: Int): TermName = TermName(s"n$i")
  def elTerm(i: Int): TermName = TermName(s"el$i")
  def fqTerm(i: Int): TermName = TermName(s"fq$i")

  def tuplePrimitive(tpe: Type): Tree = {
    val fields: List[TupleType] = tupleFields(tpe)
    val indexedFields = fields.zipWithIndex

    val sizeComp = indexedFields.map { case (f, i) =>
      val term = elTerm(i)
      fq"""
        $term <- {
          val $term = $prefix.Primitive[${f.tpe}].serialize(source.${tupleTerm(i)}, $versionTerm)
          Some((4 + { if ($term == null) 0 else $term.remaining()}))
        }
      """
    }

    val serializedComponents = indexedFields.map { case (f, i) =>
      fq""" ${elTerm(i)} <- {
          val serialized = $prefix.Primitive[${f.tpe}].serialize(source.${tupleTerm(i)}, $versionTerm)

          val buf = if (serialized == null) {
             res.putInt(-1)
           } else {
             res.putInt(serialized.remaining())
             res.put(serialized.duplicate())
           }
        Some(buf)
      }
      """
    }

    val inputTerm = TermName("input")

    val deserializedFields = indexedFields.map { case (f, i) =>
      val tm = fieldTerm(i)
      val el = elTerm(i)
      fq"""
         ${fqTerm(i)} <- {
            val $tm = $inputTerm.getInt()
            val $el = if ($tm < 0) { null } else { $codecUtils.readBytes($inputTerm, $tm) }
            Some($prefix.Primitive[${f.tpe}].deserialize($el, $versionTerm))
         }
      """
    }

    val sumTerm = indexedFields.foldRight(q"") { case ((_, pos), acc) =>
      acc match {
        case t if t.isEmpty => q"${elTerm(pos)}"
        case _ => q"${elTerm(pos)} + $acc"
      }
    }

    val extractorTerms = indexedFields.map { case (_, i) => fqTerm(i) }
    val fieldExtractor = q"for (..$deserializedFields) yield new $tpe(..$extractorTerms)"

    val tree = q"""new $prefix.Primitive[$tpe] {
      override def dataType: $strType = {
        $builder.QueryBuilder.Collections
          .tupleType(..${fields.map(_.cassandraType)})
          .queryString
      }

      override def serialize($sourceTerm: $tpe, $versionTerm: $protocolVersion): $bufferType = {
        if ($sourceTerm == null) {
           null
        } else {
          val size = {for (..$sizeComp) yield ($sumTerm) } get

          val res = $bufferCompanion.allocate(size)
          val buf = for (..$serializedComponents) yield ()
          buf.get

          res.flip().asInstanceOf[$bufferType]
        }
      }

      override def deserialize($sourceTerm: $bufferType, $versionTerm: $protocolVersion): $tpe = {
        if ($sourceTerm == null) {
          null
        } else {
          try {
            val $inputTerm = $sourceTerm.duplicate()
            $fieldExtractor.get
          } catch {
            case e: $bufferException =>
              throw new $invalidTypeException("Not enough bytes to deserialize a tuple", e)
          }
        }
      }

      override def asCql(tp: $tpe): $strType = {
        $builder.QueryBuilder.Collections.tupled(..${fields.map(_.serializer)}).queryString
      }

      override def frozen: $boolType = true

      override def shouldFreeze: $boolType = true
    }"""

    if (showTrees) c.echo(c.enclosingPosition, showCode(tree))

    tree
  }

  def mapPrimitive(tpe: Type): Tree = {
    tpe.typeArgs match {
      case k :: v :: Nil => q"""$prefix.Primitives.map[$k, $v]"""
      case _ => c.abort(c.enclosingPosition, "Expected exactly two type arguments to be provided to map")
    }
  }

  def setPrimitive(tpe: Type): Tree = {
    tpe.typeArgs.headOption match {
      case Some(inner) => q"$prefix.Primitives.set[$inner]"
      case None => c.abort(c.enclosingPosition, "Expected inner type to be defined")
    }
  }

  def optionPrimitive(tpe: Type): Tree = {
    tpe.typeArgs match {
      case head :: Nil => q"$prefix.Primitives.option[$head]"
      case _ => c.abort(
        c.enclosingPosition,
        s"Expected a single type argument for optional primitive for type ${printType(tpe)}, got ${tpe.typeArgs.size}"
      )
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

  def derivePrimitive(sourceTpe: Type): Tree = {
    val wkType = sourceTpe
    val tpe = wkType.typeSymbol

    val tree = tpe match {
      case _ if isTuple(wkType) => tuplePrimitive(wkType)
      case _ if isOption(wkType) => optionPrimitive(wkType)
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
      case Symbols.enum => enumPrimitive(wkType)
      case Symbols.enumValue => enumValuePrimitive(wkType)
      case Symbols.listSymbol => listPrimitive(wkType)
      case Symbols.setSymbol => setPrimitive(wkType)
      case Symbols.mapSymbol => mapPrimitive(wkType)
      case _ => c.abort(c.enclosingPosition, s"Cannot find primitive implementation for $tpe")
    }

    tree
  }

  def materializer[T : c.WeakTypeTag]: Tree = {
    val tpe = weakTypeOf[T]
    memoize[Type, Tree](BlackboxToolbelt.primitiveCache)(tpe, derivePrimitive)
  }
}
