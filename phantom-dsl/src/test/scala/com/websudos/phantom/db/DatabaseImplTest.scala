package com.websudos.phantom.db

import com.websudos.phantom.dsl._
import com.websudos.phantom.testkit.suites.PhantomCassandraTestSuite
import com.websudos.util.testing._

class DatabaseImplTest extends PhantomCassandraTestSuite {
  val db = new TestDatabase
  val db2 = new ValueInitDatabase

  it should "instantiate a database and collect references to the tables" in {
    db.tables.size shouldEqual 4
  }

  it should "automatically generate the CQL schema and initialise tables " in {
    db.autocreate().future().successful {
      res => {
        res.nonEmpty shouldEqual true
      }
    }
  }

  ignore should "instantiate a database object and collect references to value fields" in {
    db2.tables.size shouldEqual 4
  }

  ignore should "automatically generate the CQL schema and initialise tables for value tables" in {
    db2.autocreate().future().successful {
      res => {
        res.nonEmpty shouldEqual true
      }
    }
  }
}
