/*
 *
 *  * Copyright 2014 websudos ltd.
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

import com.websudos.util.testing._


class ZookeeperConnectorTest extends FlatSpec with Matchers with BeforeAndAfterAll with CassandraSetup {
  val instance = new ZookeeperInstance()

  override def beforeAll(): Unit = {
    super.beforeAll()
    setupCassandra()
    instance.start()
    DefaultZookeeperManagers.defaultManager.initIfNotInited(TestTable.keySpace)
  }

  override def afterAll(): Unit = {
    super.afterAll()
    instance.stop()
  }

  it should "correctly use the default localhost:2181 connector address if no environment variable has been set" in {
    System.setProperty(TestTable.manager.envString, "")

    TestTable.manager.defaultZkAddress.getHostName shouldEqual "0.0.0.0"

    TestTable.manager.defaultZkAddress.getPort shouldEqual 2181

  }

  it should "use the values from the environment variable if they are set" in {
    System.setProperty(TestTable.manager.envString, "localhost:4902")

    TestTable.manager.defaultZkAddress.getHostName shouldEqual "localhost"

    TestTable.manager.defaultZkAddress.getPort shouldEqual 4902
  }

  it should "return the default if the environment property is in invalid format" in {

    System.setProperty(TestTable.manager.envString, "localhost:invalidint")

    TestTable.manager.defaultZkAddress.getHostName shouldEqual "0.0.0.0"

    TestTable.manager.defaultZkAddress.getPort shouldEqual 2181
  }

  it should "correctly retrieve the Cassandra series of ports from the Zookeeper cluster" in {
    instance.richClient.getData(TestTable.zkPath, watch = false) successful {
      res => {
        info("Ports correctly retrieved from Cassandra.")
        new String(res.data) shouldEqual s"localhost:${DefaultCassandraManager.cassandraPort}"
      }
    }
  }

  it should "match the Zookeeper connector string to the spawned instance settings" in {
    System.setProperty(TestTable.manager.envString, instance.zookeeperConnectString)
    TestTable.manager.defaultZkAddress shouldEqual instance.address
  }

  it should "correctly retrieve the Sequence of InetSocketAddresses from zookeeper" in {

    TestTable.manager.store.hostnamePortPairs.successful {
      res => {
        res shouldEqual Seq(new InetSocketAddress("localhost", DefaultCassandraManager.cassandraPort))
      }
    }
  }
}
