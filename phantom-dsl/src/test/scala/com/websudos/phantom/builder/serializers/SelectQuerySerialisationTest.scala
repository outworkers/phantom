package com.websudos.phantom.builder.serializers

import java.util.Date

import com.websudos.phantom.builder.query.QueryBuilderTest
import com.websudos.phantom.tables.{TimeSeriesTable, BasicTable}
import com.websudos.phantom.dsl._
import com.websudos.util.testing._

class SelectQuerySerialisationTest extends QueryBuilderTest {

  "The select query builder" - {
    "should serialize " - {

      "an allow filtering clause in the init position" in {
        val id = gen[UUID]

        val qb = BasicTable.select.where(_.id eqs id).allowFiltering().limit(5).queryString

        qb shouldEqual s"SELECT * FROM phantom.BasicTable WHERE id = ${id.toString} LIMIT 5 ALLOW FILTERING;"
      }

      "an allow filtering clause specified after a limit query" in {
        val id = gen[UUID]

        val qb = BasicTable.select.where(_.id eqs id).limit(5).allowFiltering().queryString

        qb shouldEqual s"SELECT * FROM phantom.BasicTable WHERE id = ${id.toString} LIMIT 5 ALLOW FILTERING;"
      }

      "a maxTimeuuid comparison clause" in {
        val date = new Date

        val qb = TimeSeriesTable.select.where(_.timestamp > maxTimeuuid(date)).queryString

        qb shouldEqual s"SELECT * FROM phantom.TimeSeriesTable WHERE unixTimestamp > maxTimeuuid(${DateIsPrimitive.asCql(date)});"
      }

      "a maxTimeuuid comparison clause with a DateTime object" in {
        val date = new DateTime

        val qb = TimeSeriesTable.select.where(_.timestamp > maxTimeuuid(date)).queryString

        qb shouldEqual s"SELECT * FROM phantom.TimeSeriesTable WHERE unixTimestamp > maxTimeuuid(${DateTimeIsPrimitive.asCql(date)});"
      }

      "a minTimeuuid comparison clause" in {
        val date = new Date

        val qb = TimeSeriesTable.select.where(_.timestamp > minTimeuuid(date)).queryString

        qb shouldEqual s"SELECT * FROM phantom.TimeSeriesTable WHERE unixTimestamp > minTimeuuid(${DateIsPrimitive.asCql(date)});"
      }

      "a minTimeuuid comparison clause with a DateTime object" in {
        val date = new DateTime

        val qb = TimeSeriesTable.select.where(_.timestamp > minTimeuuid(date)).queryString

        qb shouldEqual s"SELECT * FROM phantom.TimeSeriesTable WHERE unixTimestamp > minTimeuuid(${DateTimeIsPrimitive.asCql(date)});"
      }
    }
  }

}
