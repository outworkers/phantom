package com.websudos.phantom

import com.websudos.phantom.tables.BasicTable
import org.scalatest.{FlatSpec, Matchers}

class ManagerTest extends FlatSpec with Matchers with PhantomKeySpace {

  ignore should "The tables are defined in the test environment" in {
    (Manager.tableList.size > 0 ) shouldBe true
  }

  ignore should "Manually adding references to tables should work" in {

    val size = Manager.tableList.size

    Manager.addTable(BasicTable)

    Manager.tableList.size shouldEqual (size + 1)

  }

}
