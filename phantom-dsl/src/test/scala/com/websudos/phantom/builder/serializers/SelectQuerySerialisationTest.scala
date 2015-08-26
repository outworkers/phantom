package com.websudos.phantom.builder.serializers

import com.websudos.phantom.builder.query.QueryBuilderTest
import com.websudos.phantom.tables.BasicTable
import com.websudos.phantom.dsl._
import com.websudos.util.testing._

class SelectQuerySerialisationTest extends QueryBuilderTest {

  "The select query builder" - {
    "should serialize " - {

      "serialise an allow filtering clause in the init position" in {
        val id = gen[UUID]

        val qb = BasicTable.select.where(_.id eqs id).allowFiltering().limit(5).queryString

        qb shouldEqual s"SELECT * FROM phantom.BasicTable WHERE id = ${id.toString} LIMIT 5 ALLOW FILTERING;"
      }

      "serialize an allow filtering clause specified after a limit query" in {
        val id = gen[UUID]

        val qb = BasicTable.select.where(_.id eqs id).limit(5).allowFiltering().queryString

        qb shouldEqual s"SELECT * FROM phantom.BasicTable WHERE id = ${id.toString} LIMIT 5 ALLOW FILTERING;"
      }

      "serialize a single ordering clause" in {
        val id = gen[UUID]

        val qb = BasicTable.select.where(_.id eqs id).orderBy(_.id2.desc).queryString

        qb shouldEqual s"SELECT * FROM phantom.BasicTable WHERE id = ${id.toString} ORDER BY (id2 DESC);"
      }

      "serialize an ordering by multiple columns" in {
        val id = gen[UUID]

        val qb = BasicTable.select.where(_.id eqs id).orderBy(_.id2.desc, _.id3.asc).queryString

        qb shouldEqual s"SELECT * FROM phantom.BasicTable WHERE id = ${id.toString} ORDER BY (id2 DESC, id3 ASC);"
      }
    }
  }

}
