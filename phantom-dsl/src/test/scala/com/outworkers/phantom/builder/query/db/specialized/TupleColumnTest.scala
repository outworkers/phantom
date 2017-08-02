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
package com.outworkers.phantom.builder.query.db.specialized

import com.outworkers.phantom.PhantomSuite
import com.outworkers.phantom.tables.{NestedTupleRecord, TupleCollectionRecord, TupleRecord}
import com.outworkers.util.samplers._
import com.outworkers.phantom.dsl._

class TupleColumnTest extends PhantomSuite {
  override def beforeAll(): Unit = {
    super.beforeAll()
    database.tuple2Table.createSchema()
    database.nestedTupleTable.createSchema()
    database.tupleCollectionsTable.createSchema()
  }

  it should "store and retrieve a record with a tuple column" in {
    val sample = gen[TupleRecord]

    val insert = database.tuple2Table.store(sample)

    val chain = for {
      store <- insert.future()
      rec <- database.tuple2Table.findById(sample.id)
    } yield rec

    whenReady(chain) { res =>
      res shouldBe defined
      res.value shouldEqual sample
    }
  }

  it should "update the value of a tuple column" in {
    val sample = gen[TupleRecord]
    val sample2 = gen[TupleRecord].copy(id = sample.id)

    val insert = database.tuple2Table.store(sample)

    val chain = for {
      store <- insert.future()
      rec <- database.tuple2Table.findById(sample.id)
      update <- database.tuple2Table.update.where(_.id eqs sample.id).modify(_.tp setTo sample2.tp).future()
      rec2 <- database.tuple2Table.findById(sample.id)
    } yield (rec, rec2)

    whenReady(chain) { case (beforeUpdate, afterUpdate) =>
      beforeUpdate shouldBe defined
      beforeUpdate.value shouldEqual sample

      afterUpdate shouldBe defined
      afterUpdate.value.tp shouldEqual sample2.tp
    }
  }

  it should "store and retrieve a record with a nested tuple column" in {
    val sample = gen[NestedTupleRecord]

    val insert = database.nestedTupleTable.store(sample)

    val chain = for {
      store <- insert.future()
      rec <- database.nestedTupleTable.findById(sample.id)
    } yield rec

    whenReady(chain) { res =>
      res shouldBe defined
      res.value shouldEqual sample
    }
  }

  it should "update the value of a nested tuple column" in {
    val sample = gen[NestedTupleRecord]
    val sample2 = gen[NestedTupleRecord].copy(id = sample.id)

    val insert = database.nestedTupleTable.store(sample)

    val chain = for {
      store <- insert.future()
      rec <- database.nestedTupleTable.findById(sample.id)
      update <- database.nestedTupleTable.update
        .where(_.id eqs sample.id)
        .modify(_.tp setTo sample2.tp)
        .future()
      rec2 <- database.nestedTupleTable.findById(sample.id)
    } yield (rec, rec2)

    whenReady(chain) { case (beforeUpdate, afterUpdate) =>
      beforeUpdate shouldBe defined
      beforeUpdate.value shouldEqual sample

      afterUpdate shouldBe defined
      afterUpdate.value.tp shouldEqual sample2.tp
    }
  }


  it should "store and retrieve a record with a collection tuple column" in {
    val sample = gen[TupleCollectionRecord]

    val insert = database.tupleCollectionsTable.store(sample)

    val chain = for {
      store <- insert.future()
      rec <- database.tupleCollectionsTable.findById(sample.id)
    } yield rec

    whenReady(chain) { res =>
      res shouldBe defined
      res.value.id shouldEqual sample.id
      res.value.tuples should contain theSameElementsAs sample.tuples
    }
  }

  it should "update the value of a collection tuple column" in {
    val sample = gen[TupleCollectionRecord]

    val appended = gen[Int] -> gen[String]

    val insert = database.tupleCollectionsTable.store(sample)

    val chain = for {
      store <- insert.future()
      rec <- database.tupleCollectionsTable.findById(sample.id)
      update <- database.tupleCollectionsTable.update
        .where(_.id eqs sample.id)
        .modify(_.tuples append appended)
        .future()
      rec2 <- database.tupleCollectionsTable.findById(sample.id)
    } yield (rec, rec2)

    whenReady(chain) { case (beforeUpdate, afterUpdate) =>
      beforeUpdate shouldBe defined
      beforeUpdate.value.id shouldEqual sample.id
      beforeUpdate.value.tuples should contain theSameElementsAs sample.tuples

      afterUpdate shouldBe defined
      afterUpdate.value.tuples should contain (appended)
    }
  }
}
