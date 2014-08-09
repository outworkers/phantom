/*
 *
 *  * Copyright 2014 newzly ltd.
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *     http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */

package com.websudos.phantom.zookeeper

import java.net.InetSocketAddress

import org.scalatest.{BeforeAndAfterAll, FlatSpec, Matchers}

import com.newzly.util.testing.AsyncAssertionsHelper._


class ZookeeperConnectorTest extends FlatSpec with Matchers with BeforeAndAfterAll with CassandraSetup {
  val instance = new ZookeeperInstance()

  override def beforeAll(): Unit = {
    super.beforeAll()
    setupCassandra()
    instance.start()
  }

  override def afterAll(): Unit = {
    super.afterAll()
    instance.stop()
  }

  it should "correctly use the default localhost:2181 connector address if no environment variable has been set" in {
    System.setProperty(TestTable.manager.envString, "")

    TestTable.manager.defaultZkAddress.getHostName shouldEqual "localhost"

    TestTable.manager.defaultZkAddress.getPort shouldEqual 2181

  }

  it should "use the values from the environment variable if they are set" in {
    System.setProperty(TestTable.manager.envString, "localhost:4902")

    TestTable.manager.defaultZkAddress.getHostName shouldEqual "localhost"

    TestTable.manager.defaultZkAddress.getPort shouldEqual 4902
  }

  it should "return the default if the environment property is in invalid format" in {

    System.setProperty(TestTable.manager.envString, "localhost:invalidint")

    TestTable.manager.defaultZkAddress.getHostName shouldEqual "localhost"

    TestTable.manager.defaultZkAddress.getPort shouldEqual 2181
  }

  it should "correctly retrieve the Cassandra series of ports from the Zookeeper cluster" in {
    instance.richClient.getData(TestTable.zkPath, watch = false) successful {
      res => {
        info("Ports correctly retrieved from Cassandra.")
        new String(res.data) shouldEqual "localhost:9142"
      }
    }
  }

  it should "match the Zookeeper connector string to the spawned instance settings" in {
    System.setProperty(TestTable.manager.envString, instance.zookeeperConnectString)
    TestTable.manager.defaultZkAddress shouldEqual instance.address
  }

  it should "correctly retrieve the Sequence of InetSocketAddresses from zookeeper" in {
    val pairs = TestTable.manager.store.hostnamePortPairs

    TestTable.manager.store.zkClient.getData(TestTable.zkPath, watch = false).successful {
      res => {
        val data = new String(res.data)
        data shouldEqual "localhost:9142"
        Console.println(pairs)
        pairs shouldEqual Seq(new InetSocketAddress("localhost", 9142))
      }
    }
  }

  it should "correctly parse multiple pairs of hostname:port from Zookeeper" in {
    val chain = for {
      set <- TestTable.manager.store.zkClient.setData(TestTable.zkPath, "localhost:9142, localhost:9900, 127.131.211.23:3402".getBytes, -1)
      get <- TestTable.manager.store.zkClient.getData("/cassandra", watch = false)
    } yield new String(get.data)

    chain.successful {
      res => {
        res shouldNot equal(null)
      }
    }
  }

  it should "use the same Zookeeper connector and client instance for all tables" in {
    TestTable.manager.store.zkClient eq TestTable2.manager.store.zkClient shouldEqual true
  }



}
