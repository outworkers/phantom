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

import scala.collection.JavaConverters._
import scala.concurrent.blocking

import com.datastax.driver.core.{Cluster, Session}
import com.twitter.conversions.time._
import com.twitter.finagle.exp.zookeeper.ZooKeeper
import com.twitter.util.{Await, Try}
import com.websudos.phantom.CassandraTable




trait ZookeeperConnector {

  self: CassandraTable[_, _] =>

  protected[zookeeper] val envString = "TEST_ZOOKEEPER_CONNECTOR"

  protected[this] val defaultAddress = new InetSocketAddress("localhost", 2181)

  val zkPath = "/cassandra"

  def zkAddress: InetSocketAddress

  val cluster: Cluster

  def connectorString = s"${zkAddress.getHostName}:${zkAddress.getPort}"

  lazy val client = ZooKeeper.newRichClient(connectorString)

  implicit lazy val session: Session = blocking {
    cluster.connect()
  }

}

trait DefaultZookeeperConnector extends ZookeeperConnector {

  self : CassandraTable[_, _] =>

  def zkAddress: InetSocketAddress = if (System.getProperty(envString) != null) {
    val inetPair: String = System.getProperty(envString)
    val split = inetPair.split(":")

    Try {
      ZookeeperManager.logger.info(s"Using ZooKeeper settings from the $envString environment variable")
      ZookeeperManager.logger.info(s"Connecting to ZooKeeper address: ${split(0)}:${split(1)}")
      new InetSocketAddress(split(0), split(1).toInt)
    } getOrElse {
      ZookeeperManager.logger.warn(s"Failed to parse address from $envString environment variable with value: $inetPair")
      defaultAddress
    }
  } else {
    ZookeeperManager.logger.info(s"No custom settings for Zookeeper found in $envString. Using localhost:2181 as defaults.")
    defaultAddress
  }

  lazy val hostnamePortPairs: Seq[InetSocketAddress] = Try {
    val res = new String(Await.result(client.getData(zkPath, watch = false), 3.seconds).data)

    res.split("\\s*,\\s*").map(_.split(":")).map {
      case Array(hostname, port) => new InetSocketAddress(hostname, port.toInt)
    }.toSeq

  } getOrElse Seq.empty[InetSocketAddress]

  lazy val cluster = Cluster.builder()
    .addContactPointsWithPorts(hostnamePortPairs.asJava)
    .withoutJMXReporting()
    .withoutMetrics()
    .build()

}
