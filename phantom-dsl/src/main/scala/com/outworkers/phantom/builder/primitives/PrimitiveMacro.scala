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

import java.nio.BufferUnderflowException

import com.datastax.driver.core.exceptions.InvalidTypeException
import com.outworkers.phantom.macros.toolbelt.BlackboxToolbelt
import scala.reflect.macros.blackbox

class PrimitiveMacro(override val c: blackbox.Context) extends BlackboxToolbelt {
  import c.universe._

  private[this] val protocolVersion = tq"_root_.com.datastax.driver.core.ProtocolVersion"
  private[this] val versionTerm = q"version"

  private[this] val boolType = typeOf[scala.Boolean]
  private[this] val strType = typeOf[java.lang.String]
  private[this] val bufferType = typeOf[_root_.java.nio.ByteBuffer]
  private[this] val bufferCompanion = q"_root_.java.nio.ByteBuffer"

  private[this] val bufferException = typeOf[BufferUnderflowException]
  private[this] val invalidTypeException = typeOf[InvalidTypeException]

  private[this] val codecUtils = q"_root_.com.datastax.driver.core.CodecUtils"
  private[this] val builder = q"_root_.com.outworkers.phantom.builder"

  private[this] val prefix = q"_root_.com.outworkers.phantom.builder.primitives"

  def typed[A : c.WeakTypeTag]: Symbol = weakTypeOf[A].typeSymbol

  def isTuple(tpe: Type): Boolean = {
    tpe.typeSymbol.fullName startsWith "scala.Tuple"
  }

  def isOption(tpe: Type): Boolean = {
    tpe.typeSymbol.fullName startsWith "scala.Option"
  }

  def printType(tpe: Type): String = showCode(tq"${tpe.dealias}")

  object Symbols {
    val listSymbol: Symbol = typed[scala.collection.immutable.List[_]]
    val setSymbol: Symbol = typed[scala.collection.immutable.Set[_]]
    val mapSymbol: Symbol = typed[scala.collection.immutable.Map[_, _]]
    val enumValue: Symbol = typed[Enumeration#Value]
    val enum: Symbol = typed[Enumeration]
  }


  def listPrimitive(tpe: Type): Tree = {
    val innerTpe = tpe.typeArgs.headOption

    innerTpe match {
      case Some(inner) => q"""$prefix.Primitives.list[$inner]()"""
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

    q"""new $prefix.Primitive[$tpe] {
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
  }

  def setPrimitive(tpe: Type): Tree = {
    tpe.typeArgs.headOption match {
      case Some(inner) => q"$prefix.Primitives.set[$inner]()"
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
      case Symbols.enum => enumPrimitive(wkType)
      case Symbols.enumValue => enumValuePrimitive(wkType)
      case Symbols.listSymbol => listPrimitive(wkType)
      case Symbols.setSymbol => setPrimitive(wkType)
      case _ => c.abort(
        c.enclosingPosition,
        s"""
          Cannot derive or find primitive implementation for $tpe.
          |Please create a Primitive manually using Primitive.iso or make sure
          |the implicit Primitve for $tpe is imported in the right scope.
        """.stripMargin
      )
    }

    evalTree(tree)
  }

  def materializer[T : c.WeakTypeTag]: Tree = {
    val tpe = weakTypeOf[T]
    memoize[Type, Tree](BlackboxToolbelt.primitiveCache)(tpe, derivePrimitive)
  }
}
