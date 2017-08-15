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
package com.outworkers.phantom.builder.query.db.crud

import com.outworkers.phantom.PhantomFreeSuite
import com.outworkers.phantom.dsl._

class CreateTest extends PhantomFreeSuite {

  "The create query builder" - {

    "should freeze collections used as part of the primary key" - {
      "freeze a list column used as part of a partition key" in {

        val query = db.primaryCollectionsTable.create.ifNotExists()

        val chain = for {
          drop <- db.primaryCollectionsTable.alter.dropIfExists().future()
          create <- query.future()
        } yield create

        whenReady(chain) { res =>
          res.forall(_.wasApplied()) shouldEqual true
        }
      }
    }

    "should generate CQL queries for custom caching properties" - {
      "serialize and create a table with Caching.None" in {

        val query = db.timeSeriesTable
          .create.`with`(caching eqs Caching.None())

        info(query.queryString)

        val chain = for {
          drop <- db.timeSeriesTable.alter.dropIfExists().future()
          create <- query.future()
        } yield create

        whenReady(chain) { res =>
          res.forall(_.wasApplied())
        }
      }

      "serialize and create a table with Caching.KeysOnly" in {

        val query = db.timeSeriesTable
          .create.`with`(caching eqs Caching.KeysOnly())

        info(query.queryString)

        val chain = for {
          drop <- db.timeSeriesTable.alter.dropIfExists().future()
          create <- query.future()
        } yield create

        whenReady(chain) { res =>
          res.forall(_.wasApplied())
        }
      }

      "serialize and create a table with Caching.RowsOnly" in {

        val query = db.timeSeriesTable
          .create.`with`(caching eqs Caching.RowsOnly())

        info(query.queryString)

        val chain = for {
          drop <- database.timeSeriesTable.alter.dropIfExists().future()
          create <- query.future()
        } yield create

        whenReady(chain) { res =>
          res.forall(_.wasApplied())
        }
      }

      "serialize and create a table with Caching.All" in {
        val query = db.timeSeriesTable
          .create.`with`(caching eqs Caching.All())

        val chain = for {
          drop <- db.timeSeriesTable.alter.dropIfExists().future()
          create <- query.future()
        } yield create

        whenReady(chain) { res =>
          res.forall(_.wasApplied()) shouldEqual true
        }
      }
    }
  }

}
