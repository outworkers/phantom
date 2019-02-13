/*
 * Copyright 2013 - 2019 Outworkers Ltd.
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
package com.outworkers.phantom.builder.serializers

import com.outworkers.phantom.builder.QueryBuilder
import com.outworkers.phantom.builder.query.QueryBuilderTest
import com.outworkers.phantom.dsl.?

class DeleteQueryBuilderTest extends QueryBuilderTest {

  "The DELETE query builder" - {

    "should allow specifying column delete queries" in {
      val qb = QueryBuilder.Delete.deleteColumn("table", "col").queryString
      qb shouldEqual "DELETE col FROM table"
    }

    "should allow specifying full delete queries" in {
      val qb = QueryBuilder.Delete.delete("table").queryString
      qb shouldEqual "DELETE FROM table"
    }

    "should create prepared delete query conditions" in {
      val qb = QueryBuilder.Delete.deletePrepared("table", "column", ?).queryString
      qb shouldEqual "column[?] FROM table"
    }

  }
}
