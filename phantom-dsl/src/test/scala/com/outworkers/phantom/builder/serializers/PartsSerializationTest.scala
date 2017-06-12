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
import com.outworkers.phantom.builder.query._
import com.outworkers.phantom.builder.query.engine.CQLQuery
import org.scalatest.FreeSpec
import com.outworkers.util.samplers._

class PartsSerializationTest extends FreeSpec with SerializationTest {

  "The query builder parts mechanism" - {

    "used through the default QueryPart interface should" - {

      "initialize the inner query list to the empty list" in {
        val part = new WherePart()

        part.queries.isEmpty shouldEqual true
        part.queries.isEmpty shouldEqual true
      }

      "correctly identify the parts lit as non empty" in {
        val list = genList[CQLQuery]()
        val part = new WherePart(list)

        part.queries.nonEmpty shouldEqual true
      }

      "append a query to the inner list while preserving order" in {
        val part = new WherePart()

        val l1 = QueryBuilder.Update.where(QueryBuilder.Where.eqs("a", "b"))

        val l2 = QueryBuilder.Update.where(QueryBuilder.Where.eqs("c", "d"))

        val appended = part.append(l1).append(l2)

        appended.queries shouldEqual List(l1, l2)
      }

      "append a sequence of queries to the inner list using varags" in {
        val part = new WherePart()

        val l1 = QueryBuilder.Update.where(QueryBuilder.Where.eqs("a", "b"))

        val l2 = QueryBuilder.Update.where(QueryBuilder.Where.eqs("c", "d"))

        val l3 = QueryBuilder.Update.where(QueryBuilder.Where.eqs("e", "f"))
        val l4 = QueryBuilder.Update.where(QueryBuilder.Where.eqs("g", "h"))

        val appended = part.append(l1).append(Seq(l2, l3, l4))

        appended.queries shouldEqual List(l1, l2, l3, l4)
      }


      "build to a CQLQuery clause with an empty init" in {
        val part = new WherePart()

        val l1 = QueryBuilder.Update.where(QueryBuilder.Where.eqs("a", "b"))

        val l2 = QueryBuilder.Update.and(QueryBuilder.Where.eqs("c", "d"))

        val appended = part.append(l1).append(l2)

        appended.qb.queryString shouldEqual "WHERE a = b AND c = d"
      }

      "build to a CQLQuery clause using a SELECT query as an initializer" in {
        val part = new WherePart()

        val init = QueryBuilder.Select.select("table", "keyspace")

        val l1 = QueryBuilder.Update.where(QueryBuilder.Where.eqs("a", "b"))

        val l2 = QueryBuilder.Update.and(QueryBuilder.Where.eqs("c", "d"))

        val appended = part.append(l1).append(l2)

        (appended build init).queryString shouldEqual "SELECT * FROM keyspace.table WHERE a = b AND c = d"
      }
    }

    "used through the set part interface should" - {

      "chain queries with a comma space separator instead of the default single space" in {
        val part = new SetPart()

        val l1 = QueryBuilder.Update.setTo("a", "b")
        val l2 = QueryBuilder.Where.eqs("c", "d")

        val appended = part.append(l1).append(l2)

        (appended qb).queryString shouldEqual "SET a = b, c = d"

        (appended build CQLQuery.empty).queryString shouldEqual "SET a = b, c = d"
      }
    }

    "compose query parts by merging them" - {

      "should merge a query part with an empty second query part and no initialisation" in {
        val part1 = new SetPart()
          .append(QueryBuilder.Update.setTo("a", "b"))
          .append(QueryBuilder.Update.setTo("c", "d"))

        (part1 merge new SetPart()).build.queryString shouldEqual "SET a = b, c = d"
      }

      "should merge a query part with an empty second query part and an init string" in {
        val part1 = new SetPart()
          .append(QueryBuilder.Update.setTo("a", "b"))
          .append(QueryBuilder.Update.setTo("c", "d"))

        (part1 merge new SetPart()).build(QueryBuilder.Update.update("k.t")).queryString shouldEqual "UPDATE k.t SET a = b, c = d"
      }

      "should merge a SET part with an WHERE part in an Update clause and no init value" in {
        val part1 = new SetPart()
          .append(QueryBuilder.Update.setTo("a", "b"))
          .append(QueryBuilder.Update.setTo("c", "d"))


        val wherePart = new WherePart().append(QueryBuilder.Update.where(QueryBuilder.Where.eqs("z1", "z2")))

        (part1 merge wherePart).build.queryString shouldEqual "SET a = b, c = d WHERE z1 = z2"
      }

      "should merge a SET part with an WHERE part in an Update clause and an init value" in {
        val part1 = new SetPart()
          .append(QueryBuilder.Update.setTo("a", "b"))
          .append(QueryBuilder.Update.setTo("c", "d"))


        val wherePart = new WherePart().append(QueryBuilder.Update.where(QueryBuilder.Where.eqs("z1", "z2")))

        (part1 merge wherePart)
          .build(QueryBuilder.Update.update("k.t"))
          .queryString shouldEqual "UPDATE k.t SET a = b, c = d WHERE z1 = z2"
      }
    }

    "merge any number of query parts using the MergePart interface" - {

      "merging consecutive empty parts with no initialization should build into the empty string" in {
        val merged = (SetPart.empty merge WherePart.empty merge OrderPart.empty merge FilteringPart.empty).build

        merged.nonEmpty shouldEqual false
      }

      "merging consecutive empty parts with an initialization should build into the initialization string" in {

        val qb = QueryBuilder.Select.count("t", "k")

        val merged = (SetPart.empty merge WherePart.empty merge OrderPart.empty merge FilteringPart.empty).build(qb)

        merged shouldEqual qb
      }

      "should merge 3 query parts using a merge part" in {
        val part1 = new SetPart()
          .append(QueryBuilder.Update.setTo("a", "b"))
          .append(QueryBuilder.Update.setTo("c", "d"))


        val wherePart = new WherePart().append(QueryBuilder.Update.where(QueryBuilder.Where.eqs("z1", "z2")))

        val filteringPart = new FilteringPart().append(QueryBuilder.Select.allowFiltering())

        (part1 merge wherePart merge filteringPart)
          .build(QueryBuilder.Select.select("t", "k"))
          .queryString shouldEqual "SELECT * FROM k.t SET a = b, c = d WHERE z1 = z2 ALLOW FILTERING"
      }
    }

  }
}
