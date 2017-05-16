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
package com.outworkers.phantom.thrift

import com.twitter.scrooge.CompactThriftSerializer
import scala.reflect.macros.blackbox

trait ThriftHelper[ValueType <: ThriftStruct] {
  def serializer: CompactThriftSerializer[ValueType]
}

object ThriftHelper {
  def apply[T <: ThriftStruct](implicit ev: ThriftHelper[T]): ThriftHelper[T] = ev

  implicit def materializer[T <: ThriftStruct]: ThriftHelper[T] = macro ThriftHelperMacro.materialize[T]
}

@macrocompat.bundle
class ThriftHelperMacro(val c: blackbox.Context) {

  import c.universe._

  private[this] val pkgRoot = q"_root_.com.outworkers.phantom.thrift"
  private[this] val scroogePkg = q"_root_.com.twitter.scrooge"
  private[this] val serializerTpe: Type => Tree = t => tq"$scroogePkg.CompactThriftSerializer[$t]"

  def materialize[T <: ThriftStruct : WeakTypeTag]: Tree = {
    val tpe = weakTypeOf[T]
    val companion = tpe.typeSymbol.companion

    q"""
      new $pkgRoot.ThriftHelper[$tpe] {
        override val serializer: ${serializerTpe(tpe)} = new $scroogePkg.CompactThriftSerializer[$tpe] {
          override val codec: $scroogePkg.ThriftStructCodec[$tpe] = $companion
        }
      }
    """
  }

}