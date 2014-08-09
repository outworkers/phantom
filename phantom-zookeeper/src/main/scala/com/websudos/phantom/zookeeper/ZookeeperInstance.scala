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
import java.util.concurrent.atomic.AtomicBoolean

import org.apache.zookeeper.server.persistence.FileTxnSnapLog
import org.apache.zookeeper.server.{NIOServerCnxn, ZKDatabase, ZooKeeperServer}

import com.twitter.common.io.FileUtils.createTempDir
import com.twitter.common.quantity.{Amount, Time}
import com.twitter.common.zookeeper.{ServerSetImpl, ZooKeeperClient}
import com.twitter.conversions.time._
import com.twitter.finagle.exp.zookeeper.ZooKeeper
import com.twitter.finagle.zookeeper.ZookeeperServerSetCluster
import com.twitter.util.{Await, Future, RandomSocket, Try}

class ZookeeperInstance(val address: InetSocketAddress = RandomSocket.nextAddress()) {

  private[this] val status = new AtomicBoolean(false)

  def isStarted: Boolean = status.get()

  val zookeeperAddress = address
  val zookeeperConnectString  = zookeeperAddress.getHostName + ":" + zookeeperAddress.getPort
  val defaultZookeeperConnectorString = "localhost:2181"

  protected[this] val envString = "TEST_ZOOKEEPER_CONNECTOR"

  val zkPath = "/cassandra"

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

  def resetEnvironment(cn: String = zookeeperConnectString): Unit = {
    System.setProperty(envString, cn)
  }

  def start() {
    if (status.compareAndSet(false, true)) {
      resetEnvironment()
      connectionFactory.startup(zookeeperServer)

      zookeeperClient = new ZooKeeperClient(
        Amount.of(10, Time.MILLISECONDS),
        zookeeperAddress)

      val serverSet = new ServerSetImpl(zookeeperClient, zkPath)
      val cluster: ZookeeperServerSetCluster = new ZookeeperServerSetCluster(serverSet)

      cluster.join(zookeeperAddress)

      Await.ready(richClient.connect(2.seconds), 2.seconds)
      Await.ready(richClient.setData(zkPath, s"localhost:${DefaultCassandraManager.cassandraPort}".getBytes, -1), 3.seconds)

      // Disable noise from zookeeper logger
      java.util.logging.LogManager.getLogManager.reset()
    } else {
      resetEnvironment(defaultZookeeperConnectorString)
    }
  }

  def stop() {
    if (status.compareAndSet(true, false)) {
      connectionFactory.shutdown()
      zookeeperClient.close()
      Await.ready(richClient.close(), 2.seconds)
    }
  }

  def hostnamePortPairs: Future[Seq[InetSocketAddress]] = richClient.getData(zkPath, watch = false) map {
    res => Try {
      val data = new String(res.data)
      data.split("\\s*,\\s*").map(_.split(":")).map {
        case Array(hostname, port) => new InetSocketAddress(hostname, port.toInt)
      }.toSeq
    } getOrElse Seq.empty[InetSocketAddress]
  }
}
