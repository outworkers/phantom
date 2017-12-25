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
package com.outworkers.phantom.builder.primitives

import com.outworkers.phantom.PhantomSuite
import com.outworkers.phantom.dsl._
import com.outworkers.phantom.tables.NestedCollections
import com.outworkers.util.samplers._

class NestedPrimitivesTest extends PhantomSuite {

  override def beforeAll(): Unit = {
    super.beforeAll()
    val _ = db.nestedCollectionTable.createSchema()
  }

  it should "automatically store a nested primitive record" in {
    val sample = gen[NestedCollections]

    val chain = for {
      store <- db.nestedCollectionTable.storeRecord(sample)
      retrieve <- db.nestedCollectionTable.select.where(_.id eqs sample.id).one()
    } yield retrieve

    whenReady(chain) { rec =>
      rec shouldBe defined
      rec.value shouldEqual sample
    }
  }

  it should "update the value of an entire record inside a nested list field" in {
    val sample = gen[NestedCollections]
    val updated = genList[List[String]]()

    val chain = for {
      store <- db.nestedCollectionTable.storeRecord(sample)
      retrieve <- db.nestedCollectionTable.select.where(_.id eqs sample.id).one()
      update <- db.nestedCollectionTable.update
        .where(_.id eqs sample.id)
        .modify(_.nestedList setTo updated)
        .future()
      retrieve2 <- db.nestedCollectionTable.select.where(_.id eqs sample.id).one()
    } yield (retrieve, retrieve2)

    whenReady(chain) { case (beforeUpdate, afterUpdate) =>
      beforeUpdate shouldBe defined
      beforeUpdate.value shouldEqual sample

      afterUpdate shouldBe defined
      afterUpdate.value.nestedList should contain theSameElementsAs updated
    }
  }

  it should "update the value of an entire record inside a nested list set field" in {
    val sample = gen[NestedCollections]
    val updated = genList[Set[String]]()

    val chain = for {
      store <- db.nestedCollectionTable.storeRecord(sample)
      retrieve <- db.nestedCollectionTable.select.where(_.id eqs sample.id).one()
      update <- db.nestedCollectionTable.update
        .where(_.id eqs sample.id)
        .modify(_.nestedListSet setTo updated)
        .future()
      retrieve2 <- db.nestedCollectionTable.select.where(_.id eqs sample.id).one()
    } yield (retrieve, retrieve2)

    whenReady(chain) { case (beforeUpdate, afterUpdate) =>
      beforeUpdate shouldBe defined
      beforeUpdate.value shouldEqual sample

      afterUpdate shouldBe defined
      afterUpdate.value.nestedListSet should contain theSameElementsAs updated
    }
  }
}
