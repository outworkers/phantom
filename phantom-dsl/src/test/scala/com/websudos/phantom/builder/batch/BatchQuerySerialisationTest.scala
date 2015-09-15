package com.websudos.phantom.builder.batch

import org.scalatest.FlatSpec

import com.websudos.phantom.builder.query.SerializationTest
import com.websudos.phantom.dsl._

import com.websudos.phantom.tables.{JodaRow, PrimitivesJoda}
import com.websudos.util.testing._

class BatchQuerySerialisationTest extends FlatSpec with SerializationTest {

  ignore should "serialize a multiple table batch query applied to multiple statements" in {

    val row = gen[JodaRow]
    val row2 = gen[JodaRow].copy(pkey = row.pkey)
    val row3 = gen[JodaRow]

    val statement3 = PrimitivesJoda.update
      .where(_.pkey eqs row2.pkey)
      .modify(_.intColumn setTo row2.int)
      .and(_.timestamp setTo row2.bi)

    val statement4 = PrimitivesJoda.delete
      .where(_.pkey eqs row3.pkey)

    val batch = Batch.logged.add(statement3, statement4)

    batch.queryString shouldEqual s"BEGIN BATCH UPDATE phantom.PrimitivesJoda SET intColumn = ${row2.int}, timestamp = ${row2.bi.getMillis} WHERE pkey = '${row2.pkey}'; DELETE FROM phantom.PrimitivesJoda WHERE pkey = '${row3.pkey}'; APPLY BATCH;"
  }

  ignore should "serialize a multiple table batch query chained from adding statements" in {

    val row = gen[JodaRow]
    val row2 = gen[JodaRow].copy(pkey = row.pkey)
    val row3 = gen[JodaRow]

    val statement3 = PrimitivesJoda.update
      .where(_.pkey eqs row2.pkey)
      .modify(_.intColumn setTo row2.int)
      .and(_.timestamp setTo row2.bi)

    val statement4 = PrimitivesJoda.delete
      .where(_.pkey eqs row3.pkey)

    val batch = Batch.logged.add(statement3).add(statement4)
    batch.queryString shouldEqual s"BEGIN BATCH UPDATE phantom.PrimitivesJoda SET intColumn = ${row2.int}, timestamp = ${row2.bi.getMillis} WHERE pkey = '${row2.pkey}'; DELETE FROM phantom.PrimitivesJoda WHERE pkey = '${row3.pkey}'; APPLY BATCH;"
  }

}
