package com.websudos.phantom.builder.serializers

import com.websudos.phantom.builder.query.QueryBuilderTest
import com.websudos.phantom.tables.BasicTable
import com.websudos.phantom.dsl._
import com.websudos.util.testing._

class DeleteQuerySerialisationTest extends QueryBuilderTest {

  "The DELETE query builder" - {
    "should generate table column deletion queries" - {
      "should create a delete query for a single column" - {
        val id = gen[UUID]
        val qb = BasicTable.delete.where(_.id eqs id).qb.queryString

        qb shouldEqual s"DELETE FROM ${keySpace.name}.${BasicTable.tableName} WHERE id = ${id.toString}"
      }

      "should create a conditional delete query if an onlyIf clause is used" in {
        val id = gen[UUID]

        val qb = BasicTable.delete.where(_.id eqs id)
          .onlyIf(_.placeholder is "test")
          .qb.queryString

        qb shouldEqual s"DELETE FROM ${keySpace.name}.${BasicTable.tableName} WHERE id = ${id.toString} IF placeholder = 'test'"
      }

      "should serialise a deleteColumn query, equivalent to an ALTER DROP" in {
        val id = gen[UUID]

        val qb = BasicTable.delete(_.placeholder).where(_.id eqs id)
          .qb.queryString

        qb shouldEqual s"DELETE placeholder FROM ${keySpace.name}.${BasicTable.tableName} WHERE id = ${id.toString}"
      }

      "should serialise a delete column query with a conditional clause" in {
        val id = gen[UUID]

        val qb = BasicTable.delete(_.placeholder).where(_.id eqs id)
          .onlyIf(_.placeholder is "test")
          .qb.queryString

        qb shouldEqual s"DELETE placeholder FROM ${keySpace.name}.${BasicTable.tableName} WHERE id = ${id.toString} IF placeholder = 'test'"
      }
    }
  }
}
