/*
 * Copyright 2013-2015 Websudos, Limited.
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * - Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 *
 * - Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *
 * - Explicit consent must be obtained from the copyright owner, Websudos Limited before any redistribution is made.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
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
