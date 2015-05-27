/*
 * Copyright 2014-2015 Sphonic Ltd. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.websudos.phantom.zookeeper

import com.datastax.driver.core.Cluster
import com.websudos.phantom.connectors.KeySpaceBuilder
import com.twitter.finagle.exp.zookeeper.ZooKeeper
import com.twitter.finagle.exp.zookeeper.client.ZkClient
import com.twitter.conversions.time._
import com.twitter.util.Future
import com.twitter.util.Try
import com.twitter.util.Await
import com.twitter.util.Duration
import scala.collection.JavaConverters._
import java.net.InetSocketAddress

/**
 * Performs the lookup of Cassandra nodes
 * in Zookeeper. Implemented as function
 * `Cluster.Builder => Cluster.Builder` so that it can
 * be easily plugged into a `KeySpaceBuilder`.
 */
class ZkContactPointLookup (host: String,
                            port: Int,
                            path: String = "/cassandra",
                            timeout: Duration = 5.seconds) extends (Cluster.Builder => Cluster.Builder) {

  def apply(builder: Cluster.Builder): Cluster.Builder = {
    val ports = Await.result(retrieveContactPoints, timeout)
    builder.addContactPointsWithPorts(ports.asJava)
  }


  /**
   * Exposes the instance of the Finagle
   * Zookeeper API that is used to perform the lookup.
   */
  protected[this] lazy val zkClient: ZkClient = {
    val conn = s"$host:$port"
    val client = ZooKeeper.newRichClient(conn)
    Await.result(client.connect(), timeout)
    client
  }

  /**
   * Performs the actual lookup of Cassandra nodes
   * in Zookeeper.
   */
  protected[this] def retrieveContactPoints: Future[Seq[InetSocketAddress]] =
    zkClient.getData(path, watch = false) map {
      res => Try {
        parseContactPoints(new String(res.data))
      } getOrElse Seq.empty[InetSocketAddress]
    }

  /**
   * Parses the specified string into a sequence
   * of InetSocketAddress instances. The default
   * implementation expects a comma
   * separated list of host/port pairs separated by a colon.
   */
  protected[this] def parseContactPoints(data: String): Seq[InetSocketAddress] = {
    data.split("\\s*,\\s*").map(_.split(":")).map {
      case Array(hostname, portDef) => new InetSocketAddress(hostname, portDef.toInt)
    }.toSeq
  }

}

/**
 * Entry point for defining a keySpace based
 * on looking up Cassandra nodes in Zookeeper.
 *
 * The default Zookeeper path being used for the lookup
 * is `/cassandra` and the expected format is a comma
 * separated list of host/port pairs separated by a colon.
 */
object ZkContactPointLookup {

  /**
   * Zookeeper's default port.
   */
  val defaultPort = 2181

  /**
   * A keyspace builder based on looking up
   * contact points in Zookeeper running on the default
   * port on localhost.
   */
  lazy val local = apply(defaultPort)

  /**
   * A keyspace builder based on looking up
   * contact points in Zookeeper running on the specified
   * port on localhost.
   */
  def apply(port: Int): KeySpaceBuilder = apply("localhost", port)

  /**
   * A keyspace builder based on looking up
   * contact points in Zookeeper running on the specified
   * host and port.
   */
  def apply(host: String, port: Int): KeySpaceBuilder =
    new KeySpaceBuilder(new ZkContactPointLookup(host,port))

  /**
   * A keyspace builder based on looking up
   * contact points in Zookeeper running on the specified
   * host and port.
   *
   * @param host the host Zookeeper runs on
   * @param port the port Zookeeper runs on
   * @param path the Zookeeper path to use for looking up Cassandra nodes
   * @param timeout the timeout for performing the lookup
   */
  def apply(host: String, port: Int, path: String, timeout: Duration): KeySpaceBuilder =
    new KeySpaceBuilder(new ZkContactPointLookup(host, port, path, timeout))

}
