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
package com.outworkers.phantom.builder.query.engine

abstract class QueryPart[T <: QueryPart[T, QT], QT <: AbstractQuery[QT]](val list: List[QT] = Nil) {

  def instance(l: List[QT]): T

  def nonEmpty: Boolean = list.nonEmpty

  def qb: QT

  def build(init: QT): QT = if (init.nonEmpty) {
    qb.bpad.prepend(init)
  } else {
    qb.prepend(init)
  }

  def append(q: QT): T = instance(list ::: (q :: Nil))

  def append(q: QT*): T = instance(q.toList ::: list)

  def append(q: List[QT]): T = instance(q ::: list)

  def mergeList(list: List[QT]): MergedQueryList[QT]

  def merge[X <: QueryPart[X, QT]](part: X): MergedQueryList[QT] = {
    val list = if (part.qb.nonEmpty) List(qb, part.qb) else List(qb)

    mergeList(list)
  }
}


abstract class MergedQueryList[QT <: AbstractQuery[QT]](val list: List[QT]) {

  def this(query: QT) = this(List(query))

  def apply(list: List[QT]): MergedQueryList[QT]

  def apply(str: String): QT

  def build: QT = apply(list.map(_.queryString).mkString(" "))

  /**
    * This will build a merge list into a final executable query.
    * It will also prepend the CQL query passed as a parameter to the final string.
    *
    * If the current list has only empty queries to merge, the init string is return instead.
    * Alternatively, the init string is prepended after a single space.
    *
    * @param init The initialisation query of the part merge.
    * @return A final, executable CQL query with all the parts merged.
    */
  def build(init: QT): QT = if (list.exists(_.nonEmpty)) {
    build.bpad.prepend(init.queryString)
  } else {
    init
  }

  def merge[X <: QueryPart[X, QT]](part: X, init: QT = apply("")): MergedQueryList[QT] = {
    val appendable = part build init

    if (appendable.nonEmpty) {
      apply(list ::: List(appendable))
    } else {
      this
    }
  }
}
