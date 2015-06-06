package com.websudos.phantom.builder.serializers

import com.websudos.phantom.builder.QueryBuilder
import com.websudos.phantom.builder.query.Defaults.{EmptyFilteringPart, EmptyOrderPart, EmptySetPart, EmptyWherePart}
import com.websudos.phantom.builder.query._
import org.scalatest.FreeSpec

class PartsSerializationTest extends FreeSpec with SerializationTest {

  "The query builder parts mechanism" - {

    "used through the default QueryPart interface should" - {

      "initialize the inner query list to the empty list" in {
        val part = new WherePart()

        part.list.isEmpty shouldEqual true
        part.list.isEmpty shouldEqual true
      }

      "append a query to the inner list while preserving order" in {
        val part = new WherePart()

        val l1 = QueryBuilder.Update.where(QueryBuilder.Where.eqs("a", "b"))

        val l2 = QueryBuilder.Update.where(QueryBuilder.Where.eqs("c", "d"))

        val appended = part.append(l1).append(l2)

        appended.list shouldEqual List(l1, l2)
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

        (appended qb).queryString shouldEqual "a = b, c = d"

        (appended build CQLQuery.empty).queryString shouldEqual "a = b, c = d"
      }
    }

    "compose query parts by merging them" - {

      "should merge a query part with an empty second query part and no initialisation" in {
        val part1 = new SetPart()
          .append(QueryBuilder.Update.setTo("a", "b"))
          .append(QueryBuilder.Update.setTo("c", "d"))

        (part1 merge new SetPart()).build.queryString shouldEqual "a = b, c = d"
      }

      "should merge a query part with an empty second query part and an init string" in {
        val part1 = new SetPart()
          .append(QueryBuilder.Update.set(QueryBuilder.Update.setTo("a", "b")))
          .append(QueryBuilder.Update.setTo("c", "d"))

        (part1 merge new SetPart()).build(QueryBuilder.Update.update("k.t")).queryString shouldEqual "UPDATE k.t SET a = b, c = d"
      }

      "should merge a SET part with an WHERE part in an Update clause and no init value" in {
        val part1 = new SetPart()
          .append(QueryBuilder.Update.set(QueryBuilder.Update.setTo("a", "b")))
          .append(QueryBuilder.Update.setTo("c", "d"))


        val wherePart = new WherePart().append(QueryBuilder.Update.where(QueryBuilder.Where.eqs("z1", "z2")))

        (part1 merge wherePart).build.queryString shouldEqual "SET a = b, c = d WHERE z1 = z2"
      }

      "should merge a SET part with an WHERE part in an Update clause and an init value" in {
        val part1 = new SetPart()
          .append(QueryBuilder.Update.set(QueryBuilder.Update.setTo("a", "b")))
          .append(QueryBuilder.Update.setTo("c", "d"))


        val wherePart = new WherePart().append(QueryBuilder.Update.where(QueryBuilder.Where.eqs("z1", "z2")))

        (part1 merge wherePart)
          .build(QueryBuilder.Update.update("k.t"))
          .queryString shouldEqual "UPDATE k.t SET a = b, c = d WHERE z1 = z2"
      }
    }

    "merge any number of query parts using the MergePart interface" - {

      "merging consecutive empty parts with no initialization should build into the empty string" in {
        val merged = (EmptySetPart merge EmptyWherePart merge EmptyOrderPart merge EmptyFilteringPart).build

        merged.nonEmpty shouldEqual false
      }

      "merging consecutive empty parts with an initialization should build into the initialization string" in {

        val qb = QueryBuilder.Select.count("t", "k")

        val merged = (EmptySetPart merge EmptyWherePart merge EmptyOrderPart merge EmptyFilteringPart).build(qb)

        merged shouldEqual qb
      }

      "should merge 3 query parts using a merge part" in {
        val part1 = new SetPart()
          .append(QueryBuilder.Update.set(QueryBuilder.Update.setTo("a", "b")))
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
