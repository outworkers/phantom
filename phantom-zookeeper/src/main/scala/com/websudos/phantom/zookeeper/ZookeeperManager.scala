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

import org.slf4j.{Logger, LoggerFactory}

import com.datastax.driver.core.{Cluster, Session}
import com.twitter.conversions.time._
import com.twitter.finagle.exp.zookeeper.ZooKeeper
import com.twitter.finagle.exp.zookeeper.client.ZkClient
import com.twitter.util.{Await, Future, Try}

trait ZookeeperManager {

  protected[this] val store: ClusterStore

  def cluster: Cluster = store.cluster

  def session: Session = store.session

  val logger: Logger

  protected[zookeeper] val envString = "TEST_ZOOKEEPER_CONNECTOR"

  protected[this] val defaultAddress = new InetSocketAddress("localhost", 2181)
}

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

  private[this] var inited = false

  lazy val logger = LoggerFactory.getLogger("com.websudos.phantom.zookeeper")

  def hostnamePortPairs: Future[Seq[InetSocketAddress]] = {
    if (inited) {
      zkClientStore.getData("/cassandra", watch = false) map {
        res => Try {
          val data = new String(res.data)
          data.split("\\s*,\\s*").map(_.split(":")).map {
            case Array(hostname, port) => new InetSocketAddress(hostname, port.toInt)
          }.toSeq
        } getOrElse Seq.empty[InetSocketAddress]
      }
    } else {
      Future.exception(new EmptyClusterStoreException())
    }
  }

  def isInited: Boolean = Lock.synchronized {
    inited
  }

  def setInited(value: Boolean) = Lock.synchronized {
    inited = value
  }

  def initStore(keySpace: String, address: InetSocketAddress ): Unit = Lock.synchronized {
    assert(address != null)

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
        val s = cluster.connect()
        s.execute(s"CREATE KEYSPACE IF NOT EXISTS $keySpace WITH replication = {'class': 'SimpleStrategy', 'replication_factor' : 1};")
        s.execute(s"use $keySpace;")
        s
      }
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

class DefaultZookeeperManager extends ZookeeperManager {

  /**
   * This is the default way a ZooKeeper connector will obtain the HOST:IP port of the ZooKeeper coordinator(master) node.
   * The phantom testing utilities are capable of auto-generating a ZooKeeper instance if none is found running.
   *
   * A test instance is ephemeral with zero persistence, it will get created, populated and deleted once per test run.
   * Upon creation, the test instance will propagate the IP:PORT combo it found available to an environment variable.
   * By convention that variable is TEST_ZOOKEEPER_CONNECTOR.
   *
   * This method will try to read that variable and parse an {@link java.net.InetSocketAddress} from it.
   * If the environment variable is null or an InetSocketAddress cannot be parsed from it, the ZooKeeper default, localhost:2181 will be used.
   * @return The InetSocketAddress of the ZooKeeper master node.
   */
  def defaultZkAddress: InetSocketAddress = if (System.getProperty(envString) != null) {
    val inetPair: String = System.getProperty(envString)
    val split = inetPair.split(":")

    Try {
      logger.info(s"Using ZooKeeper settings from the $envString environment variable")
      logger.info(s"Connecting to ZooKeeper address: ${split(0)}:${split(1)}")
      new InetSocketAddress(split(0), split(1).toInt)
    } getOrElse {
      logger.warn(s"Failed to parse address from $envString environment variable with value: $inetPair")
      defaultAddress
    }
  } else {
    logger.info(s"No custom settings for Zookeeper found in $envString. Using localhost:2181 as default.")
    defaultAddress
  }

  lazy val logger = LoggerFactory.getLogger("com.websudos.phantom.zookeeper")

  val store = DefaultClusterStore

  /**
   * This will initialise the Cassandra cluster connection based on the ZooKeeper connector settings.
   * It will connector to ZooKeeper, fetch the Cassandra sequence of HOST:IP pairs, and create a cluster + session for the mix.
   * @param connector The ZooKeeper connector instance, where the ZooKeeper address and Cassandra keySpace is specified.
   */
  def initIfNotInited(connector: ZookeeperConnector, address: InetSocketAddress = defaultZkAddress) = store.initStore(connector.keySpace, address)
}

object DefaultZookeeperManagers {
  lazy val defaultManager = new DefaultZookeeperManager
}
