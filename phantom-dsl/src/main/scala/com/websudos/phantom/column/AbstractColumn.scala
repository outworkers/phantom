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
 * - Explicit consent must be obtained from the copyright owner, Websudos Limited before any redistribution is made.
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
package com.websudos.phantom.column

import com.websudos.phantom.builder.query.CQLQuery

import scala.reflect.runtime.{currentMirror => cm}

sealed trait CassandraWrites[T] {

  /**
   * Provides the serialisation mechanism of a value to a CQL string.
   * The vast majority of serializers are fed in via the Primitives mechanism.
   *
   * Primitive columns will automatically override and define "asCql" based on the
   * serialization of specific primitives. When T is context bounded by a primitive:
   *
   * {{{
   *   def asCql(v: T): String = implicitly[Primitive[T]].asCql(value)
   * }}}
   *
   * @param v The value of the object to convert to a string.
   * @return A string that can be directly appended to a CQL query.
   */
  def asCql(v: T): String

  def cassandraType: String
}

trait AbstractColumn[@specialized(Int, Double, Float, Long, Boolean, Short) T] extends CassandraWrites[T] {

  type Value = T

  private[phantom] val isPrimary = false
  private[phantom] val isSecondaryKey = false
  private[phantom] val isPartitionKey = false
  private[phantom] val isCounterColumn = false
  private[phantom] val isStaticColumn = false
  private[phantom] val isClusteringKey = false
  private[phantom] val isAscending = false
  private[phantom] val isMapKeyIndex = false
  private[phantom] val isMapEntryIndex = false

  private[this] lazy val _name: String = {
    cm.reflect(this).symbol.name.toTypeName.decodedName.toString
  }

  def name: String = _name

  def qb: CQLQuery = {
    CQLQuery(name).forcePad.append(cassandraType)
  }

}

