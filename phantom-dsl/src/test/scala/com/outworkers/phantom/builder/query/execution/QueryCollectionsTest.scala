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
package com.outworkers.phantom.builder.query.execution

import com.outworkers.phantom.builder.query.QueryOptions
import com.outworkers.phantom.builder.query.engine.CQLQuery
import org.scalatest.{FlatSpec, Matchers}
import com.outworkers.util.samplers._

class QueryCollectionsTest extends FlatSpec with Matchers {

  it should "create a simple collection of queries from an input source" in {
    val source = genSet[String]().map(CQLQuery.apply)
    val col = new QueryCollection[Set](source.map(ExecutableCqlQuery.apply(_, QueryOptions.empty)))

    col.size shouldEqual source.size
    col.queries.map(_.qb) should contain theSameElementsAs source
  }

  it should "append new elements to an existing QueryCollection" in {
    val source = genSet[String]().map(CQLQuery.apply)
    val appendable = genSet[String]().map(CQLQuery.apply)
    val col = new QueryCollection[Set](source.map(ExecutableCqlQuery.apply(_, QueryOptions.empty)))
    val colAppendable = new QueryCollection[Set](appendable.map(ExecutableCqlQuery.apply(_, QueryOptions.empty)))

    col.size shouldEqual source.size
    col.queries.map(_.qb) should contain theSameElementsAs source

    val colFinal = col ++ colAppendable

    colFinal.size shouldEqual (source.size + appendable.size)
    colFinal.queries.map(_.qb) should contain theSameElementsAs (source ++ appendable)
  }

  it should "append another collection to an existing QueryCollection" in {
    val source = genSet[String]().map(CQLQuery.apply)
    val appendable = genSet[String]().map(CQLQuery.apply)
    val col = new QueryCollection[Set](source.map(ExecutableCqlQuery.apply(_, QueryOptions.empty)))
    val colAppendable = appendable.map(ExecutableCqlQuery.apply(_, QueryOptions.empty))

    col.size shouldEqual source.size
    col.queries.map(_.qb) should contain theSameElementsAs source

    val colFinal = col appendAll colAppendable

    colFinal.size shouldEqual (source.size + appendable.size)
    colFinal.queries.map(_.qb) should contain theSameElementsAs (source ++ appendable)
  }
}
