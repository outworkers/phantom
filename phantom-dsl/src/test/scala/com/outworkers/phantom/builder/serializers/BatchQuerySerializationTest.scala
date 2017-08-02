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
package com.outworkers.phantom.builder.serializers

import com.outworkers.phantom.builder.QueryBuilder
import com.outworkers.phantom.builder.query.QueryBuilderTest
import com.outworkers.phantom.builder.syntax.CQLSyntax.Batch

class BatchQuerySerializationTest extends QueryBuilderTest {

  "The Batch query builder" - {

    "should serialize basic batch queries" - {

      "should produce a batch query given a LOGGED batch type" in {
        val qb = QueryBuilder.Batch.batch(Batch.Logged).queryString

        qb shouldEqual s"BEGIN ${Batch.Logged} BATCH"
      }

      "should produce a batch query given an UNLOGGED batch type" in {
        val qb = QueryBuilder.Batch.batch(Batch.Unlogged).queryString

        qb shouldEqual s"BEGIN ${Batch.Unlogged} BATCH"
      }

      "should produce a batch query given an COUNTER batch type" in {
        val qb = QueryBuilder.Batch.batch(Batch.Counter).queryString

        qb shouldEqual s"BEGIN ${Batch.Counter} BATCH"
      }
    }

    "should serialize an APPLY batch query" - {

      "should produce an applied batch query given a LOGGED batch type" in {
        val qb = QueryBuilder.Batch.batch(Batch.Logged)
        val applied = QueryBuilder.Batch.applyBatch(qb).queryString

        applied shouldEqual s"BEGIN ${Batch.Logged} BATCH APPLY BATCH;"
      }


      "should produce an applied batch query given an UNLOGGED batch type" in {
        val qb = QueryBuilder.Batch.batch(Batch.Unlogged)
        val applied = QueryBuilder.Batch.applyBatch(qb).queryString

        applied shouldEqual s"BEGIN ${Batch.Unlogged} BATCH APPLY BATCH;"
      }

      "should produce an applied batch query given an COUNTER batch type" in {
        val qb = QueryBuilder.Batch.batch(Batch.Counter)
        val applied = QueryBuilder.Batch.applyBatch(qb).queryString

        applied shouldEqual s"BEGIN ${Batch.Counter} BATCH APPLY BATCH;"
      }
    }
  }
}
