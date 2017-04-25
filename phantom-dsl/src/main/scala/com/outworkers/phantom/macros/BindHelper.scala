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
package com.outworkers.phantom.macros

import com.datastax.driver.core.{BoundStatement, PreparedStatement, ProtocolVersion}

import scala.reflect.macros.whitebox

trait BindHelper[TP] {
  def bind(ps: BoundStatement, values: TP, version: ProtocolVersion): BoundStatement

  def bind(ps: PreparedStatement, values: TP, version: ProtocolVersion): BoundStatement = {
    bind(new BoundStatement(ps), values, version)
  }
}

object BindHelper {

  def apply[TP](implicit ev: BindHelper[TP]): BindHelper[TP] = ev

  implicit def materialize[TP]: BindHelper[TP] = macro BindMacros.materialize[TP]
}


@macrocompat.bundle
class BindMacros(val c: whitebox.Context) {

  import c.universe._

  private[this] val prefix = q"_root_.com.outworkers.phantom.builder.primitives"
  private[this] val boundTpe = tq"_root_.com.datastax.driver.core.BoundStatement"
  private[this] val protocolVersion = tq"_root_.com.datastax.driver.core.ProtocolVersion"

  def tupleTerm(index: Int, aug: Int = 1): TermName = {
    TermName("_" + (index + aug).toString)
  }

  def materialize[TP : WeakTypeTag]: Tree = {
    val tpe = weakTypeOf[TP]

    val source = TermName("ps")
    val values = TermName("values")
    val version = TermName("version")

    val setters = tpe.typeArgs.zipWithIndex.map { case (tp, i) =>
      q"""
        $source.setBytesUnsafe(
          $i,
          $prefix.Primitive[$tp].serialize($values.${tupleTerm(i)}, $version)
        )
      """
    }

    val tree = q"""
       new com.outworkers.phantom.macros.BindHelper[$tpe] {
          def bind($source: $boundTpe, $values: $tpe, $version: $protocolVersion): $boundTpe = {
            ..$setters
            $source
          }
       }
    """
    Console.println(showCode(tree))
    tree
  }

}