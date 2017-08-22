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
package com.outworkers.phantom.builder.serializers

import com.outworkers.phantom.builder.QueryBuilder
import com.outworkers.phantom.builder.primitives.Primitive
import com.outworkers.phantom.builder.query.SerializationTest
import com.outworkers.phantom.builder.syntax.CQLSyntax
import org.scalatest.{FlatSpec, Matchers}

class DataTypeSerializationTest extends FlatSpec with Matchers with SerializationTest {

  it should "add a static column modifier on a simple collection" in {
    val stringP = Primitive[String]

    db.staticCollectionTable.staticList.cassandraType shouldEqual s"list<${stringP.dataType}> static"
  }

  it should "freeze a collection type without a frozen inner type" in {
    val qb = QueryBuilder.Collections.collectionType(
      colType = CQLSyntax.Collections.list,
      cassandraType = CQLSyntax.Types.BigInt,
      shouldFreeze = true,
      freezeInner = false,
      static = false
    ).queryString

    qb shouldEqual "frozen<list<bigint>>"
  }

  it should "freeze a collection type with a frozen inner type" in {

    val tpe = QueryBuilder.Collections.tupleType(
      CQLSyntax.Types.BigInt,
      CQLSyntax.Types.Text
    ).queryString

    val qb = QueryBuilder.Collections.collectionType(
      colType = CQLSyntax.Collections.list,
      cassandraType = tpe,
      shouldFreeze = true,
      freezeInner = true,
      static = false
    ).queryString

    qb shouldEqual "frozen<list<frozen<tuple<bigint, text>>>>"
  }

  it should "generate a frozen collection if its used as a partition key " in {

    val stringP = Primitive[String]

    val cType = db.primaryCollectionsTable.listIndex.cassandraType

    cType shouldEqual s"frozen<list<${stringP.dataType}>>"
  }

  it should "generate a frozen collection if its used as a primary key " in {
    val stringP = Primitive[String]

    val cType = db.primaryCollectionsTable.setCol.cassandraType

    cType shouldEqual s"frozen<set<${stringP.dataType}>>"
  }

  it should "generate a frozen collection type for a tuple inside a list" in {
    val innerP = Primitive[(Int, String)]
    val cType = db.tupleCollectionsTable.tuples.cassandraType

    cType shouldEqual s"list<frozen<${innerP.dataType}>>"
  }

  it should "generate a frozen collection type for a tuple inside a set" in {
    val innerP = Primitive[(Int, String)]
    val cType = db.tupleCollectionsTable.uniqueTuples.cassandraType

    cType shouldEqual s"set<frozen<${innerP.dataType}>>"
  }

  it should "generate a frozen collection type for map with a collection key and value type" in {
    val stringP = Primitive[String]
    val cType = db.nestedCollectionTable.doubleProps.cassandraType

    cType shouldEqual s"map<frozen<set<${stringP.dataType}>>, frozen<list<${stringP.dataType}>>>"
  }

  it should "generate a frozen collection type for a nested list" in {
    val stringP = Primitive[String]
    val cType = db.nestedCollectionTable.nestedList.cassandraType

    cType shouldEqual s"list<frozen<list<${stringP.dataType}>>>"
  }

  it should "generate a frozen collection type for a nested list set" in {
    val stringP = Primitive[String]
    val cType = db.nestedCollectionTable.nestedListSet.cassandraType

    cType shouldEqual s"list<frozen<set<${stringP.dataType}>>>"
  }

  it should "generate a frozen collection type for map with a collection value type" in {
    val stringP = Primitive[String]
    val cType = db.nestedCollectionTable.props.cassandraType

    cType shouldEqual s"map<text, frozen<list<${stringP.dataType}>>>"
  }

  it should "freeze nested collections properly" in {
    val stringP = Primitive[String]
    val inner = Primitive[List[String]]
    val listP = Primitive[List[List[String]]]

    inner.frozen shouldEqual true

    listP.cassandraType shouldEqual s"frozen<list<frozen<list<${stringP.dataType}>>>>"
  }
}
