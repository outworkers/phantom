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
import com.outworkers.phantom.dsl._
import com.outworkers.phantom.tables.PrimitiveRecord
import com.outworkers.util.samplers._

class ConsistencyLevelTests extends PhantomSuite {

  it should "set a custom consistency level of ONE" in {
    val row = gen[PrimitiveRecord]

    val st = database.primitives
      .delete.where(_.pkey eqs row.pkey)
      .consistencyLevel_=(ConsistencyLevel.ONE)
      .executableQuery.statement()

    if (session.protocolConsistency) {
      st.getConsistencyLevel shouldEqual ConsistencyLevel.ONE
    } else {
      st.getConsistencyLevel shouldEqual None.orNull
    }

  }

  it should "set a custom consistency level of LOCAL_ONE in a DELETE query" in {
    val row = gen[PrimitiveRecord]

    val st = database.primitives
      .delete.where(_.pkey eqs row.pkey)
      .consistencyLevel_=(ConsistencyLevel.LOCAL_ONE)
      .executableQuery
      .statement

    if (session.protocolConsistency) {
      st.getConsistencyLevel shouldEqual ConsistencyLevel.LOCAL_ONE
    } else {
      st.getConsistencyLevel shouldEqual None.orNull
    }

  }

  it should "set a custom consistency level of EACH_QUORUM in a SELECT query" in {
    val row = gen[PrimitiveRecord]

    val st = database.primitives
      .select.where(_.pkey eqs row.pkey)
      .consistencyLevel_=(ConsistencyLevel.EACH_QUORUM)
      .executableQuery
      .statement

    if (session.protocolConsistency) {
      st.getConsistencyLevel shouldEqual ConsistencyLevel.EACH_QUORUM
    } else {
      st.getConsistencyLevel shouldEqual None.orNull
    }
  }

  it should "set a custom consistency level of LOCAL_ONE in an UPDATE query" in {
    val row = gen[PrimitiveRecord]

    val st = database.primitives
      .update.where(_.pkey eqs row.pkey)
      .consistencyLevel_=(ConsistencyLevel.LOCAL_ONE)
      .executableQuery
      .statement

    if (session.protocolConsistency) {
      st.getConsistencyLevel shouldEqual ConsistencyLevel.LOCAL_ONE
    } else {
      st.getConsistencyLevel shouldEqual None.orNull
    }

  }

  it should "set a custom consistency level of QUORUM in an INSERT query" in {
    val row = gen[PrimitiveRecord]

    val st = database.primitives
      .store(row)
      .consistencyLevel_=(ConsistencyLevel.QUORUM)
      .executableQuery
      .statement

    if (session.protocolConsistency) {
      st.getConsistencyLevel shouldEqual ConsistencyLevel.QUORUM
    } else {
      st.getConsistencyLevel shouldEqual None.orNull
    }
  }

  it should "set a custom consistency level of QUORUM in a TRUNCATE query" in {
    val st = database.primitives
      .truncate
      .consistencyLevel_=(ConsistencyLevel.QUORUM)
      .executableQuery
      .statement

    if (session.protocolConsistency) {
      st.getConsistencyLevel shouldEqual ConsistencyLevel.QUORUM
    } else {
      st.getConsistencyLevel shouldEqual None.orNull
    }
  }

  ignore should "set a custom consistency level of QUORUM in a CREATE query" in {
    val st = database.primitives
      .create
      .ifNotExists()
      .consistencyLevel_=(ConsistencyLevel.LOCAL_QUORUM)
      .executableQuery
      .statement

    if (session.protocolConsistency) {
      st.getConsistencyLevel shouldEqual ConsistencyLevel.LOCAL_QUORUM
    } else {
      st.getConsistencyLevel shouldEqual None.orNull
    }
  }

}
