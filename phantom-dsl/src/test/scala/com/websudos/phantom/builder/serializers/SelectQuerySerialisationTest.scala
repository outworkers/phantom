package com.websudos.phantom.builder.serializers

import com.websudos.phantom.builder.query.QueryBuilderTest
import com.websudos.phantom.builder.query.prepared.?
import com.websudos.phantom.tables.BasicTable
import com.websudos.phantom.dsl._
import com.websudos.util.testing._

class SelectQuerySerialisationTest extends QueryBuilderTest {

  "The select query builder" - {
    "should serialize " - {

      "serialise an allow filtering clause in the init position" in {
        val id = gen[UUID]

        val qb = BasicTable.select.where(_.id eqs id).allowFiltering().limit(5).queryString

        Console.println(qb)

        qb shouldEqual s"SELECT * FROM phantom.BasicTable WHERE id = ${id.toString} LIMIT 5 ALLOW FILTERING"
      }

      "serialize an allow filtering clause specified after a limit query" in {
        val id = gen[UUID]

        val qb = BasicTable.select.where(_.id eqs id).limit(5).allowFiltering().queryString

        Console.println(qb)

        qb shouldEqual s"SELECT * FROM phantom.BasicTable WHERE id = ${id.toString} LIMIT 5 ALLOW FILTERING"
      }

      "serialize a prepared statement" in {
        val id = gen[UUID]

        val qb = BasicTable.prepare.select.where(_.id eqs ?).allowFiltering().queryString

        Console.println(qb)

        qb shouldEqual s"SELECT * FROM phantom.BasicTable WHERE id = ? LIMIT 5 ALLOW FILTERING"
      }
    }
  }

}
