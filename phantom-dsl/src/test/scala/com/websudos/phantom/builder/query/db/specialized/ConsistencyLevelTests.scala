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
package com.websudos.phantom.builder.query.db.specialized

import com.datastax.driver.core.ProtocolVersion
import com.websudos.phantom.PhantomSuite
import com.websudos.phantom.tables.{TestDatabase, Primitive}
import com.websudos.util.testing._
import com.websudos.phantom.dsl._

class ConsistencyLevelTests extends PhantomSuite {

  val protocol = session.getCluster.getConfiguration.getProtocolOptions.getProtocolVersion

  it should "set a custom consistency level of ONE" in {
    val row = gen[Primitive]

    val st = TestDatabase.primitives.delete.where(_.pkey eqs row.pkey).consistencyLevel_=(ConsistencyLevel.ONE).statement

    if (protocol.compareTo(ProtocolVersion.V2) == 1) {
      st.getConsistencyLevel shouldEqual ConsistencyLevel.ONE
    } else {
      st.getConsistencyLevel shouldEqual null
    }

  }

  it should "set a custom consistency level of LOCAL_ONE in a DELETE query" in {
    val row = gen[Primitive]

    val st = TestDatabase.primitives.delete.where(_.pkey eqs row.pkey).consistencyLevel_=(ConsistencyLevel.LOCAL_ONE).statement

    if (protocol.compareTo(ProtocolVersion.V2) == 1) {
      st.getConsistencyLevel shouldEqual ConsistencyLevel.LOCAL_ONE
    } else {
      st.getConsistencyLevel shouldEqual null
    }

  }

  it should "set a custom consistency level of EACH_QUORUM in a SELECT query" in {
    val row = gen[Primitive]

    val st = TestDatabase.primitives.select.where(_.pkey eqs row.pkey)
      .consistencyLevel_=(ConsistencyLevel.EACH_QUORUM).statement

    if (protocol.compareTo(ProtocolVersion.V2) == 1) {
      st.getConsistencyLevel shouldEqual ConsistencyLevel.EACH_QUORUM
    } else {
      st.getConsistencyLevel shouldEqual null
    }
  }

  it should "set a custom consistency level of LOCAL_ONE in an UPDATE query" in {
    val row = gen[Primitive]

    val st = TestDatabase.primitives.update.where(_.pkey eqs row.pkey)
      .consistencyLevel_=(ConsistencyLevel.LOCAL_ONE).statement

    if (protocol.compareTo(ProtocolVersion.V2) == 1) {
      st.getConsistencyLevel shouldEqual ConsistencyLevel.LOCAL_ONE
    } else {
      st.getConsistencyLevel shouldEqual null
    }

  }

  it should "set a custom consistency level of QUORUM in an INSERT query" in {
    val row = gen[Primitive]

    val st = TestDatabase.primitives.store(row).consistencyLevel_=(ConsistencyLevel.QUORUM).statement

    if (protocol.compareTo(ProtocolVersion.V2) == 1) {
      st.getConsistencyLevel shouldEqual ConsistencyLevel.QUORUM
    } else {
      st.getConsistencyLevel shouldEqual null
    }
  }

  it should "set a custom consistency level of QUORUM in a TRUNCATE query" in {
    val st = TestDatabase.primitives.truncate.consistencyLevel_=(ConsistencyLevel.QUORUM).statement

    if (protocol.compareTo(ProtocolVersion.V2) == 1) {
      st.getConsistencyLevel shouldEqual ConsistencyLevel.QUORUM
    } else {
      st.getConsistencyLevel shouldEqual null
    }
  }

  it should "set a custom consistency level of QUORUM in a CREATE query" in {
    val st = TestDatabase.primitives.create.ifNotExists()
      .consistencyLevel_=(ConsistencyLevel.LOCAL_QUORUM).statement

    if (protocol.compareTo(ProtocolVersion.V2) == 1) {
      st.getConsistencyLevel shouldEqual ConsistencyLevel.LOCAL_QUORUM
    } else {
      st.getConsistencyLevel shouldEqual null
    }
  }

}
