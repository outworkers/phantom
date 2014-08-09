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
import scala.collection.JavaConverters._

import scala.concurrent._

import org.slf4j.LoggerFactory

import com.datastax.driver.core.{Session, Cluster}
import com.twitter.finagle.exp.zookeeper.ZooKeeper
import com.twitter.finagle.exp.zookeeper.client.ZkClient
import com.twitter.conversions.time._
import com.twitter.util.{Await, Try, Future}

private[zookeeper] case object Lock

class EmptyClusterStoreException extends RuntimeException("Attempting to retrieve Cassandra cluster reference before initialisation")

/**
 * This is a simple implementation that will allow for singleton synchronisation of Cassandra clusters and sessions.
 * Connector traits may be mixed in to any number of Cassandra tables, but at runtime, the cluster, session or ZooKeeper client must be the same.
 *
 * The ClusterStore serves as a sync point for the ZooKeeperClient, Cassandra Cluster and Cassandra session triplet.
 * All it needs as external information is the keySpace used by the Cassandra connection.
 *
 * It will perform the following sequence of actions in order:
 * - It will read a known environment variable, named TEST_ZOOKEEPER_CONNECTOR to find the IP:PORT combo for the master ZooKeeper coordinator node.
 * - If the combo is not found, it will try to use the default ZooKeeper master address, namely localhost:2181.
 * - It will then try to connect to the ZooKeeper master and fetch the data from the "/cassandra" path.
 * - On that path, it expects to find a sequence of IP:PORT combos in the following format: "ip1:host1, ip2:host2, ip3:host3, ...".
 * - It will fetch the ports, parse them into their equivalent java.net.InetSocketAddress instance.
 * - With that sequence of InetSocketAddress connections, it will spawn a Cassandra Load Balancer cluster configuration.
 * - This will work with any number of Cassandra nodes present in ZooKeeper, the connection manager will load balance over all Cassandra IPs found in ZooKeeper.
 * - After the cluster is spawned, the Store will attempt to create the keySpace if necessary, using a Cassandra Compare-and-Set query.
 * - It will then feed the keySpace into the Cassandra session to obtain a final, directly usable values where queries can execute.
 *
 * The initialisation process is entirely synchronised, using the JVM handle before to ensure only the first thread trying to read a Cassandra Session will
 * cause the initialisation process to start. Any thread thereafter will simply read the initialised ready-to-use version. Any attempt to read values directly
 * before they are initialised will throw an EmptyClusterStoreException.
 */
trait ClusterStore {

  protected[this] var clusterStore: Cluster = null
  protected[this] var zkClientStore: ZkClient = null
  protected[this] var _session: Session = null
  private[this] val sessions = new scala.collection.mutable.LinkedHashMap[String, Session]() with scala.collection.mutable.SynchronizedMap[String, Session]


  private[this] var inited = false

  lazy val logger = LoggerFactory.getLogger("com.websudos.phantom.zookeeper")

  def hostnamePortPairs: Future[Seq[InetSocketAddress]] = Lock.synchronized {
    zkClientStore.getData("/cassandra", watch = false) map {
      res => Try {
        val data = new String(res.data)
        data.split("\\s*,\\s*").map(_.split(":")).map {
          case Array(hostname, port) => new InetSocketAddress(hostname, port.toInt)
        }.toSeq
      } getOrElse Seq.empty[InetSocketAddress]
    }
  }

  def isInited: Boolean = Lock.synchronized {
    inited
  }

  def setInited(value: Boolean) = Lock.synchronized {
    inited = value
  }

  def initStore(keySpace: String, address: InetSocketAddress ): Unit = Lock.synchronized {
    if (!isInited) {
      val conn = s"${address.getHostName}:${address.getPort}"
      zkClientStore = ZooKeeper.newRichClient(conn)

      Console.println(s"Connecting to ZooKeeper server instance on $conn")

      val res = Await.result(zkClientStore.connect(), 2.seconds)

      val ports = Await.result(hostnamePortPairs, 2.seconds)

      clusterStore = Cluster.builder()
        .addContactPointsWithPorts(ports.asJava)
        .withoutJMXReporting()
        .withoutMetrics()
        .build()

      _session = blocking {
        val s = clusterStore.connect()
        s.execute(s"CREATE KEYSPACE IF NOT EXISTS $keySpace WITH replication = {'class': 'SimpleStrategy', 'replication_factor' : 1};")
        s.execute(s"use $keySpace;")
        s
      }
      sessions.put(keySpace, _session)
      setInited(value = true)
    }
  }

  @throws[EmptyClusterStoreException]
  def cluster: Cluster = {
    if (isInited) {
      clusterStore
    } else {
      throw new EmptyClusterStoreException
    }
  }

  @throws[EmptyClusterStoreException]
  def session: Session = {
    if (isInited) {
      _session
    } else {
      throw new EmptyClusterStoreException
    }
  }

  @throws[EmptyClusterStoreException]
  def zkClient: ZkClient = {
    if (isInited) {
      zkClientStore
    } else {
      throw new EmptyClusterStoreException
    }
  }
}

object DefaultClusterStore extends ClusterStore
