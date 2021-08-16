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
package com.outworkers.phantom.macros

import com.datastax.driver.core.{BoundStatement, PreparedStatement, ProtocolVersion}
import com.outworkers.phantom.macros.toolbelt.WhiteboxToolbelt

import scala.annotation.implicitNotFound
import scala.reflect.macros.whitebox

@implicitNotFound(msg = "${TP} is probably missing a Primitive for one or more of the bound types.")
trait BindHelper[TP] {

  def debugString(values: TP): String

  def bind(ps: BoundStatement, values: TP, version: ProtocolVersion): BoundStatement

  def bind(ps: PreparedStatement, values: TP, version: ProtocolVersion): BoundStatement = {
    bind(new BoundStatement(ps) {
      override def toString: String = ps.getQueryString + " | " + debugString(values)
    }, values, version)
  }
}

object BindHelper {

  def apply[TP](implicit ev: BindHelper[TP]): BindHelper[TP] = ev

  implicit def materialize[TP]: BindHelper[TP] = macro BindMacros.materialize[TP]
}

class BindMacros(override val c: whitebox.Context) extends WhiteboxToolbelt with RootMacro {

  import c.universe._

  private[this] val prefix = q"_root_.com.outworkers.phantom.builder.primitives"
  private[this] val boundTpe = tq"_root_.com.datastax.driver.core.BoundStatement"
  private[this] val protocolVersion = tq"_root_.com.datastax.driver.core.ProtocolVersion"

  private[this] val source = TermName("ps")
  private[this] val value = TermName("value")
  private[this] val version = TermName("version")

  def isTuple(tpe: Type): Boolean = {
    tpe.typeSymbol.fullName startsWith "scala.Tuple"
  }

  def isCaseClass(tpe: Type): Boolean = {
    tpe.typeSymbol.isClass && tpe.typeSymbol.asClass.isCaseClass
  }

  lazy val showBoundStatements: Boolean =
    !c.inferImplicitValue(typeOf[debug.optionTypes.ShowBoundStatements], silent = true).isEmpty

  def queryString(col: Iterable[(TermName, Type)]): Tree = {
    if (showBoundStatements) {
      val steps = col.map { case (nm, tpe) => q"$prefix.Primitive[$tpe].asCql($value.$nm)" }
      q"""_root_.scala.collection.immutable.List.apply(..$steps).mkString(", ")"""
    } else {
      q"""new $strTpe("")"""
    }
  }

  def bindSingle(tpe: Type): Tree = {

    val debugTree = if (showBoundStatements) {
      q"""
        override def debugString($value: $tpe): $strTpe = {
          $prefix.Primitive[$tpe].asCql($value)
        }
      """
    } else {
      q"""
        override def debugString($value: $tpe): $strTpe = new $strTpe("")
      """
    }

    q"""
       new com.outworkers.phantom.macros.BindHelper[$tpe] {
          $debugTree

          def bind($source: $boundTpe, $value: $tpe, $version: $protocolVersion): $boundTpe = {
             $source.setBytesUnsafe(
                0,
                $prefix.Primitive[$tpe].serialize($value, $version)
             )
             $source
          }
       }
    """
  }

  def bindCaseClass(tpe: Type): Tree = {
    val fields = caseFields(tpe).map { case (nm, tp) => nm.toTermName -> tp }
    val setters = fields.zipWithIndex.map { case ((fieldName, fieldTpe), i) =>
      q"""
        $source.setBytesUnsafe(
          $i,
          $prefix.Primitive[$fieldTpe].serialize($value.$fieldName, $version)
        )
      """
    }

    q"""
       new com.outworkers.phantom.macros.BindHelper[$tpe] {

          override def debugString($value: $tpe): $strTpe = ${queryString(fields)}

          def bind($source: $boundTpe, $value: $tpe, $version: $protocolVersion): $boundTpe = {
            ..$setters
            $source
          }
       }
    """
  }

  def bindTuple(tpe: Type): Tree = {

    val fields = tpe.typeArgs.zipWithIndex.map { case (tp, i) => tupleTerm(i) -> tp }

    val setters = tpe.typeArgs.zipWithIndex.map { case (tp, i) =>
      q"""
        $source.setBytesUnsafe(
          $i,
          $prefix.Primitive[$tp].serialize($value.${tupleTerm(i)}, $version)
        )
      """
    }

    q"""
       new com.outworkers.phantom.macros.BindHelper[$tpe] {
          override def debugString($value: $tpe): $strTpe = {
            ${queryString(fields)}
          }

          def bind($source: $boundTpe, $value: $tpe, $version: $protocolVersion): $boundTpe = {
            ..$setters
            $source
          }
       }
    """
  }

  protected[this] def deriveHelper(tpe: Type): Tree = {
    if (isTuple(tpe)) {
      bindTuple(tpe)
    } else if (isCaseClass(tpe)) {
      bindCaseClass(tpe)
    } else {
      bindSingle(tpe)
    }
  }

  def materialize[TP : WeakTypeTag]: Tree = {
    val tpe = weakTypeOf[TP]
    memoize[Type, Tree](WhiteboxToolbelt.bindHelperCache)(tpe, deriveHelper)
  }
}