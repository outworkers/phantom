package com.websudos.phantom.builder.query.db.specialized

import com.datastax.driver.core.ProtocolVersion
import com.websudos.phantom.tables.{Primitives, Primitive}
import com.websudos.phantom.testkit._
import com.websudos.util.testing._
import com.websudos.phantom.dsl._

class ConsistencyLevelTests extends PhantomCassandraTestSuite {

  val protocol = session.getCluster.getConfiguration.getProtocolOptions.getProtocolVersionEnum

  it should "set a custom consistency level of ONE" in {
    val row = gen[Primitive]

    val st = Primitives.delete.where(_.pkey eqs row.pkey).consistencyLevel_=(ConsistencyLevel.ONE).statement

    if (protocol.compareTo(ProtocolVersion.V2) == 1) {
      st.getConsistencyLevel shouldEqual ConsistencyLevel.ONE
    } else {
      st.getConsistencyLevel shouldEqual null
    }

  }

  it should "set a custom consistency level of LOCAL_ONE in a DELETE query" in {
    val row = gen[Primitive]

    val st = Primitives.delete.where(_.pkey eqs row.pkey).consistencyLevel_=(ConsistencyLevel.LOCAL_ONE).statement

    if (protocol.compareTo(ProtocolVersion.V2) == 1) {
      st.getConsistencyLevel shouldEqual ConsistencyLevel.LOCAL_ONE
    } else {
      st.getConsistencyLevel shouldEqual null
    }

  }

  it should "set a custom consistency level of EACH_QUORUM in a SELECT query" in {
    val row = gen[Primitive]

    val st = Primitives.select.where(_.pkey eqs row.pkey)
      .consistencyLevel_=(ConsistencyLevel.EACH_QUORUM).statement

    if (protocol.compareTo(ProtocolVersion.V2) == 1) {
      st.getConsistencyLevel shouldEqual ConsistencyLevel.EACH_QUORUM
    } else {
      st.getConsistencyLevel shouldEqual null
    }
  }

  it should "set a custom consistency level of LOCAL_ONE in an UPDATE query" in {
    val row = gen[Primitive]

    val st = Primitives.update.where(_.pkey eqs row.pkey)
      .consistencyLevel_=(ConsistencyLevel.LOCAL_ONE).statement

    if (protocol.compareTo(ProtocolVersion.V2) == 1) {
      st.getConsistencyLevel shouldEqual ConsistencyLevel.LOCAL_ONE
    } else {
      st.getConsistencyLevel shouldEqual null
    }

  }

  it should "set a custom consistency level of QUORUM in an INSERT query" in {
    val row = gen[Primitive]

    val st = Primitives.store(row).consistencyLevel_=(ConsistencyLevel.QUORUM).statement

    if (protocol.compareTo(ProtocolVersion.V2) == 1) {
      st.getConsistencyLevel shouldEqual ConsistencyLevel.QUORUM
    } else {
      st.getConsistencyLevel shouldEqual null
    }
  }

  it should "set a custom consistency level of QUORUM in a TRUNCATE query" in {
    val st = Primitives.truncate.consistencyLevel_=(ConsistencyLevel.QUORUM).statement

    if (protocol.compareTo(ProtocolVersion.V2) == 1) {
      st.getConsistencyLevel shouldEqual ConsistencyLevel.QUORUM
    } else {
      st.getConsistencyLevel shouldEqual null
    }
  }

  it should "set a custom consistency level of QUORUM in a CREATE query" in {
    val st = Primitives.create.ifNotExists()
      .consistencyLevel_=(ConsistencyLevel.LOCAL_QUORUM).statement

    if (protocol.compareTo(ProtocolVersion.V2) == 1) {
      st.getConsistencyLevel shouldEqual ConsistencyLevel.LOCAL_QUORUM
    } else {
      st.getConsistencyLevel shouldEqual null
    }
  }

}
