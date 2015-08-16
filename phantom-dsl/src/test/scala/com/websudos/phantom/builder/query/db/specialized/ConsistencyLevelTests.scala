package com.websudos.phantom.builder.query.db.specialized

import com.websudos.phantom.tables.{Primitives, Primitive}
import com.websudos.phantom.testkit._
import com.websudos.util.testing._
import com.websudos.phantom.dsl._

class ConsistencyLevelTests extends PhantomCassandraTestSuite {

  it should "set a custom consistency level of ONE" in {
    val row = gen[Primitive]

    val st = Primitives.delete.where(_.pkey eqs row.pkey).consistencyLevel_=(ConsistencyLevel.ONE).statement

    st.getConsistencyLevel shouldEqual ConsistencyLevel.ONE

    val chain = for {
      store <- Primitives.store(row).future()
      inserted <- Primitives.select.where(_.pkey eqs row.pkey).one()
      delete <- Primitives.delete.where(_.pkey eqs row.pkey).consistencyLevel_=(ConsistencyLevel.ONE).future()
      deleted <- Primitives.select.where(_.pkey eqs row.pkey).one
    } yield (inserted, deleted, delete)

    chain successful {
      r => {
        r._1.isDefined shouldEqual true
        r._1.get shouldEqual row

        r._2.isEmpty shouldEqual true
        r._3.wasApplied() shouldEqual true
      }
    }
  }

  it should "set a custom consistency level of LOCAL_ONE in a DELETE query" in {
    val row = gen[Primitive]

    val st = Primitives.delete.where(_.pkey eqs row.pkey).consistencyLevel_=(ConsistencyLevel.LOCAL_ONE).statement

    st.getConsistencyLevel shouldEqual ConsistencyLevel.LOCAL_ONE

    val chain = for {
      store <- Primitives.store(row).future()
      inserted <- Primitives.select.where(_.pkey eqs row.pkey).one()
      delete <- Primitives.delete.where(_.pkey eqs row.pkey).consistencyLevel_=(ConsistencyLevel.LOCAL_ONE).future()
      deleted <- Primitives.select.where(_.pkey eqs row.pkey).one
    } yield (inserted, deleted, delete)

    chain successful {
      r => {
        r._1.isDefined shouldEqual true
        r._1.get shouldEqual row

        r._2.isEmpty shouldEqual true
        r._3.wasApplied() shouldEqual true
      }
    }

  }

  it should "set a custom consistency level of EACH_QUORUM in a SELECT query" in {
    val row = gen[Primitive]

    val st = Primitives.select.where(_.pkey eqs row.pkey)
      .consistencyLevel_=(ConsistencyLevel.EACH_QUORUM).statement

    st.getConsistencyLevel shouldEqual ConsistencyLevel.EACH_QUORUM
  }

  it should "set a custom consistency level of LOCAL_ONE in an UPDATE query" in {
    val row = gen[Primitive]

    val st = Primitives.update.where(_.pkey eqs row.pkey)
      .consistencyLevel_=(ConsistencyLevel.LOCAL_ONE).statement

    st.getConsistencyLevel shouldEqual ConsistencyLevel.LOCAL_ONE
  }

  it should "set a custom consistency level of QUORUM in an INSERT query" in {
    val row = gen[Primitive]

    val st = Primitives.store(row).consistencyLevel_=(ConsistencyLevel.QUORUM).statement

    st.getConsistencyLevel shouldEqual ConsistencyLevel.QUORUM
  }

  it should "set a custom consistency level of QUORUM in a TRUNCATE query" in {
    val st = Primitives.truncate.consistencyLevel_=(ConsistencyLevel.QUORUM).statement

    st.getConsistencyLevel shouldEqual ConsistencyLevel.QUORUM
  }

  it should "set a custom consistency level of QUORUM in a CREATE query" in {
    val st = Primitives.create.ifNotExists()
      .consistencyLevel_=(ConsistencyLevel.LOCAL_QUORUM).statement

    st.getConsistencyLevel shouldEqual ConsistencyLevel.LOCAL_QUORUM
  }

}
