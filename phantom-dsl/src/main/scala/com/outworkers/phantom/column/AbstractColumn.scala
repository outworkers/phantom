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
package com.outworkers.phantom.column

import com.outworkers.phantom.builder.query.engine.CQLQuery

import scala.reflect.runtime.{currentMirror => cm}

trait AbstractColumn[@specialized(Int, Double, Float, Long, Boolean, Short) T] {

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

  type Value = T

  def isPrimary: Boolean = false
  def isSecondaryKey: Boolean = false
  def isPartitionKey: Boolean = false
  def isCounterColumn: Boolean = false
  def isStaticColumn: Boolean = false
  def isClusteringKey: Boolean = false
  def isAscending: Boolean = false
  def isMapKeyIndex: Boolean = false
  def isMapEntryIndex: Boolean = false

  private[this] lazy val _name: String = {
    cm.reflect(this).symbol.name.toTypeName.decodedName.toString
  }

  def name: String = _name

  def qb: CQLQuery = CQLQuery(name).forcePad.append(cassandraType)

  /**
    * Whether or not this is a compound primitive type that should free if the
    * type of primitive is a collection.
    *
    * This means that Cassandra will serialise your collection to a blob
    * instead of a normal index based collection storage, so things like index access
    * will not be available.
    *
    * One such scenario is using a list as part of the primary key, because of how
    * Cassandra works, we need to treat the list as a blob, as if we change its contents
    * we would breach basic rules of serialisation/hashing.
    *
    * @return A boolean that says whether or not this type should be frozen.
    */
  def shouldFreeze: Boolean = isPrimary || isPartitionKey

}

