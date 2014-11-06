/*
 * Copyright 2013 websudos ltd.
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
package com.websudos.phantom.column

import scala.reflect.runtime.{currentMirror => cm, universe => ru}

import com.websudos.phantom.CassandraWrites

private[phantom] trait AbstractColumn[@specialized(Int, Double, Float, Long, Boolean, Short) T] extends CassandraWrites[T] {

  type Value = T
  private[phantom] val isPrimary = false
  private[phantom] val isSecondaryKey = false
  private[phantom] val isPartitionKey = false
  private[phantom] val isCounterColumn = false
  private[phantom] val isStaticColumn = false
  private[phantom] val isClusteringKey = false
  private[phantom] val isAscending = false

  private[this] lazy val _name: String = {
    cm.reflect(this).symbol.name.toTypeName.decoded
  }
  
  def name: String = _name
}

