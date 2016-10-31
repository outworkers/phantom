/*
 * Copyright 2013 - 2017 Outworkers Ltd.
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
package com.outworkers.phantom.connectors

/**
 * Entry point for defining a keySpace based
 * on a single contact point (Cassandra node).
 *
 * Using a single contact point only is usually
 * only recommended for testing purposes.
 */
object ContactPoint {

  private[this] val localhost = "localhost"

  /**
   * Cassandra's default ports.
   */
  object DefaultPorts {

    val live = 9042

    val embedded = 9142

  }

  /**
   * A keyspace builder based on a single
   * contact point running on the default
   * port on localhost.
   */
  lazy val local = apply(DefaultPorts.live)

  /**
   * A keyspace builder based on a single
   * contact point running on the default
   * port of embedded Cassandra.
   */
  lazy val embedded = apply(DefaultPorts.embedded)

  /**
   * A keyspace builder based on a single
   * contact point running on the specified
   * port on localhost.
   */
  def apply(port: Int): KeySpaceBuilder = apply(localhost, port)

  /**
   * A keyspace builder based on a single
   * contact point running on the specified
   * host and port.
   */
  def apply(host: String, port: Int): KeySpaceBuilder =
    new KeySpaceBuilder(_.addContactPoint(host).withPort(port))

}

/**
 * Entry point for defining a keySpace based
 * on multiple contact points (Cassandra nodes).
 *
 * Even though the Cassandra driver technically only
 * needs a single contact point and will then fetch
 * the metadata for all other Cassandra nodes, it is
 * recommended to specify more than just one contact
 * point in case one node is down the moment the driver
 * initializes.
 *
 * Since the driver finds additional nodes on its own,
 * the initial list of contact points only needs to be
 * updated when you remove one of the specified contact
 * points, not when merely adding new nodes to the cluster.
 */
object ContactPoints {

  /**
   * A keyspace builder based on the specified
   * contact points, all running on the default port.
   */
  def apply(hosts: Seq[String]): KeySpaceBuilder =
    new KeySpaceBuilder(_.addContactPoints(hosts:_*))

  /**
   * A keyspace builder based on the specified
   * contact points, all running on the specified port.
   */
  def apply(hosts: Seq[String], port: Int): KeySpaceBuilder =
    new KeySpaceBuilder(_.addContactPoints(hosts:_*).withPort(port))
}
