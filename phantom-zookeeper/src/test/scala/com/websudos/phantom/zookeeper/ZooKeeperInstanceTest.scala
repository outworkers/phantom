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

class ZooKeeperInstanceTest extends FlatSpec with Matchers with BeforeAndAfterAll {
  val instance = new ZookeeperInstance()

  override def beforeAll(): Unit = {
    super.beforeAll()
    instance.start()
  }

  override def afterAll(): Unit = {
    super.afterAll()
    instance.stop()
  }

  it should "correctly set the status flag to true after starting the ZooKeeper Instance" in {
    instance.isStarted shouldEqual true
  }

  it should "correctly initialise a ZooKeeper ServerSet after starting a ZooKeeper instance" in {
    instance.zookeeperServer.isRunning shouldEqual true
  }

  it should "retrieve the correct data from the Cassandra path by default" in {
    instance.richClient.getData("/cassandra", watch = false).successful {
      res => {
        res shouldNot equal(null)
        res.data shouldNot equal(null)
        new String(res.data) shouldEqual s"localhost:${DefaultCassandraManager.cassandraPort}"
      }

    }
  }

  it should "correctly parse the retrieved data into a Sequence of InetSocketAddresses" in {
    instance.hostnamePortPairs.successful {
      res => {
        res shouldEqual Seq(new InetSocketAddress("localhost", DefaultCassandraManager.cassandraPort))
      }
    }
  }
}
