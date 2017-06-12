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
import com.outworkers.phantom.macros.TableHelper
import com.outworkers.phantom.tables.Article
import com.outworkers.phantom.tables.sasi.SASIIndexedArticles

class SASIQueriesTest extends PhantomSuite {

  it should "automatically find SASI indexed columns " in {
    val th = TableHelper[SASIIndexedArticles, Article]
    val sasiColumns = th.sasiIndexes(database.sasiIndexedArticles)

    sasiColumns.exists(_.name == database.sasiIndexedArticles.orderId.name) shouldEqual true
  }
}
