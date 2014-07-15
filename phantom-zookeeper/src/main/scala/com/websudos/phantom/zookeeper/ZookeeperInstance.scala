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

import org.apache.zookeeper.server.persistence.FileTxnSnapLog
import org.apache.zookeeper.server.{NIOServerCnxn, ZKDatabase, ZooKeeperServer}

import com.twitter.common.io.FileUtils.createTempDir
import com.twitter.common.quantity.{Amount, Time}
import com.twitter.common.zookeeper.{ServerSetImpl, ZooKeeperClient}
import com.twitter.conversions.time._
import com.twitter.finagle.exp.zookeeper.ZooKeeper
import com.twitter.finagle.zookeeper.ZookeeperServerSetCluster
import com.twitter.util.{Await, RandomSocket}


class ZookeeperInstance(private[this] val address: InetSocketAddress = RandomSocket.nextAddress()) {

  val zookeeperAddress = address
  val zookeeperConnectString  = zookeeperAddress.getHostName + ":" + zookeeperAddress.getPort

  lazy val connectionFactory: NIOServerCnxn.Factory = new NIOServerCnxn.Factory(zookeeperAddress)
  lazy val txn = new FileTxnSnapLog(createTempDir(), createTempDir())
  lazy val zkdb = new ZKDatabase(txn)

  lazy val zookeeperServer: ZooKeeperServer = new ZooKeeperServer(
    txn,
    ZooKeeperServer.DEFAULT_TICK_TIME,
    100,
    100,
    new ZooKeeperServer.BasicDataTreeBuilder,
    zkdb
  )
  var zookeeperClient: ZooKeeperClient = null

  lazy val richClient = ZooKeeper.newRichClient(zookeeperConnectString)

  def start() {

    connectionFactory.startup(zookeeperServer)

    zookeeperClient = new ZooKeeperClient(
      Amount.of(10, Time.MILLISECONDS),
      zookeeperAddress)

    val serverSet = new ServerSetImpl(zookeeperClient, "/cassandra")
    val cluster = new ZookeeperServerSetCluster(serverSet)

    cluster.join(zookeeperAddress)

    Await.ready(richClient.connect(2.seconds), 2.seconds)
    Await.ready(richClient.setData("/cassandra", "localhost:9142".getBytes, -1), 3.seconds)

    // Disable noise from zookeeper logger
    java.util.logging.LogManager.getLogManager.reset()
  }

  def stop() {
    connectionFactory.shutdown()
    zookeeperClient.close()
  }
}
