package com.websudos.phantom

import com.websudos.phantom.testkit.suites.CassandraFeatureSpec

class ManagerTest extends CassandraFeatureSpec with PhantomKeySpace {

  feature("The manager should automatically collect references to all tables") {
    scenario("The tables are defined in the test environment") {
      (Manager.tableList.size > 0 ) shouldBe true
    }
  }
}
