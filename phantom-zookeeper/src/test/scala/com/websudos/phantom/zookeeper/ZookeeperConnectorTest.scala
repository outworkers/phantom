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
import org.scalatest.{ BeforeAndAfterAll, FlatSpec, Matchers }

import com.newzly.util.testing.AsyncAssertionsHelper._
import com.twitter.conversions.time._
import com.twitter.util.Await

object TestTable extends DefaultZookeeperConnector {
  val keySpace = "phantom"
}

object TestTable2 extends DefaultZookeeperConnector {
  val keySpace = "phantom"
}


class ZookeeperConnectorTest extends FlatSpec with Matchers with BeforeAndAfterAll {
  val instance = new ZookeeperInstance()

  override def beforeAll(): Unit = {
    super.beforeAll()
    instance.start()
  }

  override def afterAll(): Unit = {
    super.afterAll()
    instance.stop()
  }

  ignore should "correctly use the default localhost:2181 connector address if no environment variable has been set" in {
    System.setProperty(TestTable.zkManager.envString, "")

    TestTable.zkManager.defaultZkAddress.getHostName shouldEqual "localhost"

    TestTable.zkManager.defaultZkAddress.getPort shouldEqual 2181

  }

  ignore should "use the values from the environment variable if they are set" in {
    System.setProperty(TestTable.zkManager.envString, "localhost:4902")

    TestTable.zkManager.defaultZkAddress.getHostName shouldEqual "localhost"

    TestTable.zkManager.defaultZkAddress.getPort shouldEqual 4902
  }

  ignore should "return the default if the environment property is in invalid format" in {

    System.setProperty(TestTable.zkManager.envString, "localhost:invalidint")

    TestTable.zkManager.defaultZkAddress.getHostName shouldEqual "localhost"

    TestTable.zkManager.defaultZkAddress.getPort shouldEqual 2181
  }

  it should "correctly retrieve the Cassandra series of ports from the Zookeeper cluster" in {
    instance.richClient.getData(TestTable.zkPath, watch = false) successful {
      res => {
        info("Ports correctly retrieved from Cassandra.")
        new String(res.data) shouldEqual "localhost:9142"
      }
    }
  }

  ignore should "match the Zookeeper connector string to the spawned instance settings" in {
    System.setProperty(TestTable.zkManager.envString, instance.zookeeperConnectString)
    TestTable.zkManager.defaultZkAddress shouldEqual instance.address
  }

  it should "correctly retrieve the Sequence of InetSocketAddresses from zookeeper" in {
    val pairs = TestTable.zkManager.store.hostnamePortPairs

    TestTable.zkManager.store.zkClient.getData(TestTable.zkPath, watch = false).successful {
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
      set <- TestTable.zkManager.store.zkClient.setData(TestTable.zkPath, "localhost:9142, localhost:9900, 127.131.211.23:3402".getBytes, -1)
      get <- TestTable.zkManager.store.zkClient.getData("/cassandra", watch = false)
    } yield new String(get.data)

    chain.successful {
      res => {
        res shouldNot equal(null)
      }
    }
  }

  it should "use the same Zookeeper connector and client instance for all tables" in {
    TestTable.zkManager.store.zkClient eq TestTable2.zkManager.store.zkClient shouldEqual true
  }



}
