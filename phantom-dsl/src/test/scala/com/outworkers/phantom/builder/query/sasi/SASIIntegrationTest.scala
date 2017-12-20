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
package com.outworkers.phantom.builder.query.sasi

import com.outworkers.phantom.PhantomSuite
import com.outworkers.phantom.tables.sasi.MultiSASIRecord
import com.outworkers.phantom.dsl._
import com.outworkers.util.samplers._
import com.outworkers.phantom.macros.debug.Options.ShowBoundStatements

class SASIIntegrationTest extends PhantomSuite {

  override def beforeAll(): Unit = {
    super.beforeAll()

    if (cassandraVersion.value >= Version.`3.4.0`) {
      val _ = db.multiSasiTable.createSchema()
    }
  }

  it should "allow retrieving prefix results using a like operator in Mode.Prefix" in {
    val pre = gen[ShortString].value
    val samples = genList[MultiSASIRecord]().map(item => item.copy(phoneNumber = pre + item.phoneNumber))

    if (cassandraVersion.value >= Version.`3.4.0`) {
      val chain = for {
        _ <- db.multiSasiTable.storeRecords(samples)
        query <- db.multiSasiTable.select.where(_.phoneNumber like prefix(pre)).fetch()
      } yield query

      whenReady(chain) { results =>
        results should contain theSameElementsAs samples
      }
    }
  }

  it should "shouldn't allow mixing incorrect bind value types" in {
    val pre = gen[ShortString].value

    if (cassandraVersion.value >= Version.`3.4.0`) {
      val ps = db.multiSasiTable.select.where(_.phoneNumber like prefix(?)).prepareAsync()

      "ps.flatMap(_.bind(ContainsValue(pre)).fetch())" shouldNot compile
    }
  }

  it should "allow retrieving prefix results using a like operator in Mode.Prefix with prepared statements" in {
    val pre = gen[ShortString].value
    val samples = genList[MultiSASIRecord]().map(item => item.copy(phoneNumber = pre + item.phoneNumber))

    if (cassandraVersion.value >= Version.`3.4.0`) {
      val ps = db.multiSasiTable.select.where(_.phoneNumber like prefix(?)).prepareAsync()
      val chain = for {
        _ <- db.multiSasiTable.storeRecords(samples)
        query <- ps.flatMap(_.bind(PrefixValue(pre)).fetch())
      } yield query

      whenReady(chain) { results =>
        results should contain theSameElementsAs samples
      }
    }
  }

  it should "allow retrieving suffix results using a like operator in Mode.Contains" in {
    val pre = gen[ShortString].value
    val samples = genList[MultiSASIRecord]().map(item => item.copy(name = item.name + pre))

    if (cassandraVersion.value >= Version.`3.4.0`) {
      val chain = for {
        _ <- db.multiSasiTable.storeRecords(samples)
        query <- db.multiSasiTable.select.where(_.name like suffix(pre)).fetch()
      } yield query

      whenReady(chain) { results =>
        results should contain theSameElementsAs samples
      }
    }
  }


  it should "allow retrieving suffix results using a like operator in Mode.Contains and PS" in {
    val suf = gen[ShortString].value
    val samples = genList[MultiSASIRecord]().map(item => item.copy(name = item.name + suf))

    if (cassandraVersion.value >= Version.`3.4.0`) {

      val ps = db.multiSasiTable.select.where(_.name like suffix(?)).prepareAsync()
      val chain = for {
        _ <- db.multiSasiTable.storeRecords(samples)
        query <- ps.flatMap(_.bind(SuffixValue(suf)).fetch())
      } yield query

      whenReady(chain) { results =>
        results should contain theSameElementsAs samples
      }
    }
  }

  it should "allow retrieving prefix results using a like operator in Mode.Contains" in {
    val pre = gen[ShortString].value
    val samples = genList[MultiSASIRecord]().map(item => item.copy(name = pre + item.name))

    if (cassandraVersion.value >= Version.`3.4.0`) {
      val chain = for {
        _ <- db.multiSasiTable.storeRecords(samples)
        query <- db.multiSasiTable.select.where(_.name like prefix(pre)).fetch()
      } yield query

      whenReady(chain) { results =>
        results should contain theSameElementsAs samples
      }
    }
  }

  it should "allow retrieving contains results using a like operator in Mode.Contains" in {
    val pre = gen[ShortString].value
    val samples = genList[MultiSASIRecord]().map(item => item.copy(name = item.name + pre + item.name))

    if (cassandraVersion.value >= Version.`3.4.0`) {
      val chain = for {
        _ <- db.multiSasiTable.storeRecords(samples)
        query <- db.multiSasiTable.select.where(_.name like contains(pre)).fetch()
      } yield query

      whenReady(chain) { results =>
        results should contain theSameElementsAs samples
      }
    }
  }

  it should "allow retrieving contains results using a like operator in Mode.Contains and prepared statements" in {
    val pre = gen[ShortString].value
    val samples = genList[MultiSASIRecord]().map(item => item.copy(name = item.name + pre + item.name))

    if (cassandraVersion.value >= Version.`3.4.0`) {
      val chain = for {
        _ <- db.multiSasiTable.storeRecords(samples)
        source = db.multiSasiTable.select.where(_.name like contains(?))
        query <- source.prepareAsync()
        select <- query.bind(ContainsValue(pre)).fetch()
      } yield select

      whenReady(chain) { results =>
        results should contain theSameElementsAs samples
      }
    }
  }

  it should "allow retrieving gte results using a normal operator in Mode.Sparse" in {
    val pre = 55
    val samples = genList[MultiSASIRecord]().map(item => item.copy(customers = pre))

    if (cassandraVersion.value >= Version.`3.4.0`) {
      val chain = for {
        _ <- db.multiSasiTable.truncate().future()
        _ <- db.multiSasiTable.storeRecords(samples)
        query <- db.multiSasiTable.select.where(_.customers >= pre).fetch()
      } yield query

      whenReady(chain) { results =>
        results should contain theSameElementsAs samples
      }
    }
  }

  it should "allow retrieving gte results using a normal operator in Mode.Sparse using prepared statements" in {
    val pre = 55
    val samples = genList[MultiSASIRecord]().map(item => item.copy(customers = pre))

    if (cassandraVersion.value >= Version.`3.4.0`) {

      val query = db.multiSasiTable.select.where(_.customers >= ?).prepareAsync()

      val chain = for {
        _ <- db.multiSasiTable.truncate().future()
        _ <- db.multiSasiTable.storeRecords(samples)
        bindable <- query
        query <- bindable.bind(pre).fetch()
      } yield query

      whenReady(chain) { results =>
        results should contain theSameElementsAs samples
      }
    }
  }

  it should "allow retrieving lte results using a normal operator in Mode.Sparse" in {
    val pre = 55
    val samples = genList[MultiSASIRecord]().map(item => item.copy(customers = pre))

    if (cassandraVersion.value >= Version.`3.4.0`) {
      val chain = for {
        _ <- db.multiSasiTable.truncate().future()
        _ <- db.multiSasiTable.storeRecords(samples)
        query <- db.multiSasiTable.select.where(_.customers <= pre).fetch()
      } yield query

      whenReady(chain) { results =>
        results should contain theSameElementsAs samples
      }
    }
  }



  it should "allow retrieving lte results using a normal operator in Mode.Sparse and PS" in {
    val pre = 55
    val samples = genList[MultiSASIRecord]().map(item => item.copy(customers = pre))

    if (cassandraVersion.value >= Version.`3.4.0`) {

      val ps = db.multiSasiTable.select.where(_.customers <= ?).prepareAsync()

      val chain = for {
        _ <- db.multiSasiTable.truncate().future()
        _ <- db.multiSasiTable.storeRecords(samples)
        query <- ps.flatMap(_.bind(pre).fetch())
      } yield query

      whenReady(chain) { results =>
        results should contain theSameElementsAs samples
      }
    }
  }

  it should "allow retrieving == results using a normal operator in Mode.Sparse" in {
    val pre = 55
    val samples = genList[MultiSASIRecord]().map(item => item.copy(customers = pre))

    if (cassandraVersion.value >= Version.`3.4.0`) {
      val chain = for {
        _ <- db.multiSasiTable.truncate().future()
        _ <- db.multiSasiTable.storeRecords(samples)
        query <- db.multiSasiTable.select.where(_.customers eqs pre).fetch()
      } yield query

      whenReady(chain) { results =>
        results should contain theSameElementsAs samples
      }
    }
  }


  it should "allow retrieving == results using a normal operator in Mode.Sparse and PS" in {
    val pre = 55
    val samples = genList[MultiSASIRecord]().map(item => item.copy(customers = pre))

    if (cassandraVersion.value >= Version.`3.4.0`) {

      val ps = db.multiSasiTable.select.where(_.customers eqs ?).prepareAsync()

      val chain = for {
        _ <- db.multiSasiTable.truncate().future()
        _ <- db.multiSasiTable.storeRecords(samples)
        query <- ps.flatMap(_.bind(pre).fetch())
      } yield query

      whenReady(chain) { results =>
        results should contain theSameElementsAs samples
      }
    }
  }

  it should "retrieve no results for an invalid clause in Mode.Sparse" in {
    val pre = 55
    val samples = genList[MultiSASIRecord]().map(item => item.copy(customers = pre))

    if (cassandraVersion.value >= Version.`3.4.0`) {
      val chain = for {
        _ <- db.multiSasiTable.truncate().future()
        _ <- db.multiSasiTable.storeRecords(samples)
        query <- db.multiSasiTable.select.where(_.customers > pre).fetch()
      } yield query

      whenReady(chain) { results =>
        results.size shouldEqual 0
      }
    }
  }

  it should "retrieve no results for an invalid clause in Mode.Sparse and PS" in {
    val pre = 55
    val samples = genList[MultiSASIRecord]().map(item => item.copy(customers = pre))

    if (cassandraVersion.value >= Version.`3.4.0`) {
      val ps = db.multiSasiTable.select.where(_.customers > ?).prepareAsync()

      val chain = for {
        _ <- db.multiSasiTable.truncate().future()
        _ <- db.multiSasiTable.storeRecords(samples)
        query <- ps.flatMap(_.bind(pre).fetch())
      } yield query

      whenReady(chain) { results =>
        results.size shouldEqual 0
      }
    }
  }

}
