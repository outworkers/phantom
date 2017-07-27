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

abstract class QueryPart[T <: QueryPart[T]](val queries: Seq[CQLQuery] = Nil) {

  def instance(l: Seq[CQLQuery]): T

  def nonEmpty: Boolean = queries.nonEmpty

  def qb: CQLQuery

  def build(init: CQLQuery): CQLQuery = if (init.nonEmpty) {
    qb.bpad.prepend(init)
  } else {
    qb
  }

  def append(q: CQLQuery): T = instance(queries :+ q)

  def append(q: Seq[CQLQuery]): T = instance(queries ++ q)

  def append(other: T): T = instance(queries ++ other.queries)

  def mergeList(list: Seq[CQLQuery]): MergeList

  def merge[X <: QueryPart[X]](part: X): MergeList = {
    val list = if (part.qb.nonEmpty) Seq(qb, part.qb) else Seq(qb)

    mergeList(list)
  }
}


case class MergeList(queries: Seq[CQLQuery]) {

  def this(query: CQLQuery) = this(Seq(query))

  def nonEmpty: Boolean = queries.nonEmpty

  def apply(list: Seq[CQLQuery]): MergeList = new MergeList(list)

  def build: CQLQuery = CQLQuery(queries.map(_.queryString).mkString(" "))

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
  def build(init: CQLQuery): CQLQuery = if (queries.exists(_.nonEmpty)) {
    build.bpad.prepend(init.queryString)
  } else {
    init
  }

  def merge[X <: QueryPart[X]](part: X, init: CQLQuery = CQLQuery.empty): MergeList = {
    val appendable = part build init

    if (appendable.nonEmpty) {
      apply(queries :+ appendable)
    } else {
      this
    }
  }
}

object MergeList {
  def empty: MergeList = new MergeList(Nil)
}