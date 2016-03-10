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
package com.websudos.phantom.builder.query.prepared

import com.websudos.phantom.PhantomSuite
import com.websudos.phantom.codec.JodaLocalDateCodec
import com.websudos.phantom.dsl._
import com.websudos.phantom.tables.{Primitive, PrimitiveCassandra22, Recipe, TestDatabase}
import com.websudos.util.testing._

class PreparedInsertQueryTest extends PhantomSuite {

  override def beforeAll(): Unit = {
    super.beforeAll()
    TestDatabase.recipes.insertSchema()
    TestDatabase.primitives.insertSchema()
    if (session.v4orNewer) {
      TestDatabase.primitivesCassandra22.insertSchema()
    }
  }

  it should "serialize an insert query" in {

    val sample = gen[Recipe]

    val query = TestDatabase.recipes.insert
      .p_value(_.uid, ?)
      .p_value(_.url, ?)
      .p_value(_.servings, ?)
      .p_value(_.calories, ?)
      .p_value(_.ingredients, ?)
      .p_value(_.description, ?)
      .p_value(_.lastcheckedat, ?)
      .p_value(_.props, ?)
      .prepare()

    val exec = query.bind(
      sample.uid,
      sample.url,
      sample.servings,
      sample.calories,
      sample.ingredients,
      sample.description,
      sample.lastCheckedAt,
      sample.props
    ).future()

    val chain = for {
      store <- exec
      get <- TestDatabase.recipes.select.where(_.url eqs sample.url).one()
    } yield get

    whenReady(chain) {
      res => {
        res shouldBe defined
        res.value shouldEqual sample
      }
    }
  }

  it should "serialize a primitives insert query" in {
    val sample = gen[Primitive]

    val query = TestDatabase.primitives.insert
      .p_value(_.pkey, ?)
      .p_value(_.long, ?)
      .p_value(_.boolean, ?)
      .p_value(_.bDecimal, ?)
      .p_value(_.double, ?)
      .p_value(_.float, ?)
      .p_value(_.inet, ?)
      .p_value(_.int, ?)
      .p_value(_.date, ?)
      .p_value(_.uuid, ?)
      .p_value(_.bi, ?)
      .prepare()

    val exec = query.bind(
      sample.pkey,
      sample.long,
      sample.boolean,
      sample.bDecimal,
      sample.double,
      sample.float,
      sample.inet,
      sample.int,
      sample.date,
      sample.uuid,
      sample.bi
    ).future()

    val chain = for {
      store <- exec
      get <- TestDatabase.primitives.select.where(_.pkey eqs sample.pkey).one()
    } yield get

    whenReady(chain) {
      res => {
        res shouldBe defined
        res.value shouldEqual sample
      }
    }
  }

  if (session.v4orNewer) {
    it should "serialize a cassandra 2.2 primitives insert query" in {
      session.getCluster.getConfiguration.getCodecRegistry.register(new JodaLocalDateCodec)
      val sample = gen[PrimitiveCassandra22]

      val query = TestDatabase.primitivesCassandra22.insert
        .p_value(_.pkey, ?)
        .p_value(_.short, ?)
        .p_value(_.byte, ?)
        .p_value(_.date, ?)
        .prepare()

      val exec = query.bind(
        sample.pkey,
        sample.short,
        sample.byte,
        sample.localDate
      ).future()

      val selectQuery = TestDatabase.primitivesCassandra22.select
        .p_where(_.pkey eqs ?)
        .prepare()

      val chain = for {
        store <- exec
        get <- selectQuery.bind(sample.pkey).one()
      } yield get

      whenReady(chain) {
        res => {
          res shouldBe defined
          res.value shouldEqual sample
        }
      }
    }
  }
}
