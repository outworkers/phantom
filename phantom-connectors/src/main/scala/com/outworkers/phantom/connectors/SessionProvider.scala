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

import com.datastax.driver.core.{Cluster, Session}
import org.slf4j.{Logger, LoggerFactory}

/**
 * Responsible for providing Session instances of the
 * Cassandra driver for multiple keySpaces defined
 * in the same cluster.
 */
trait SessionProvider {

  lazy val logger: Logger = LoggerFactory.getLogger(this.getClass)

  def defaultKeyspaceCreationQuery(keySpace: String): String = {
    s"""
      |CREATE KEYSPACE IF NOT EXISTS $keySpace WITH replication = {
      | 'class': 'SimpleStrategy',
      | 'replication_factor' : 1
      |};
     """.stripMargin
  }

  /**
   * The Cassandra driver's Cluster instance
   * used by this provider to create new
   * Session instances.
   */
  def cluster: Cluster

  def space: KeySpace

  /**
   * Returns a Session instance for the keySpace
   * with the specified name.
   *
   * It is recommended that implementations
   * cache instances per keySpace, so that they
   * can hand out existing instances in case
   * a client asks for the same keySpace multiple
   * times.
   */
  def session: Session
}
