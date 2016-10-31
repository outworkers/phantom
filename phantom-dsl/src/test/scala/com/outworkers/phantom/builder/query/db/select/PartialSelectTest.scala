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
package com.outworkers.phantom.builder.query.db.select

import com.outworkers.phantom.PhantomSuite
import com.outworkers.phantom.dsl._
import com.outworkers.phantom.tables._
import com.outworkers.util.testing._


class PartialSelectTest extends PhantomSuite {

  override def beforeAll(): Unit = {
    super.beforeAll()
    TestDatabase.primitives.insertSchema()
  }

  "Partially selecting 2 fields" should "correctly select the fields" in {
    val row = gen[Primitive]

    val chain = for {
      truncate <- TestDatabase.primitives.truncate.future()
      insertDone <- TestDatabase.primitives.store(row).future()
      listSelect <- TestDatabase.primitives.select(_.pkey).fetch
      oneSelect <- TestDatabase.primitives.select(_.long, _.boolean).where(_.pkey eqs row.pkey).one
    } yield (listSelect, oneSelect)

    chain successful {
      case (res, res2) => {
        res shouldEqual List(row.pkey)
        res2.value shouldEqual Tuple2(row.long, row.boolean)
      }
    }
  }
}
