/*
 * Copyright 2013-2015 Websudos, Limited.
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * - Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 *
 * - Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *
 * - Explicit consent must be obtained from the copyright owner, Websudos Limited before any redistribution is made.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package com.websudos.phantom.connectors

import com.datastax.driver.core.{Cluster, Session}

trait CassandraManager {

  def livePort: Int
  def embeddedPort: Int

  def cluster: Cluster
  def initIfNotInited(keySpace: String)

  def ZookeeperEnvironmentString: String = "TEST_ZOOKEEPER_CONNECTOR"

  implicit def session: Session

  /**
   * Creates the CQL query to be executed when phantom connectors guarantee the existence of the keySpace before connection.
   * By default, this will use lightweight transactions in Cassandra(IF NOT EXISTS queries) to guarantee data is not overwritten.
   *
   * @param keySpace The string name of the KeySpace the manager needs to use.
   * @return The CQL Query that will be executed to create the KeySpace.
   */
  protected[this] def createKeySpace(keySpace: String): String = {
    s"CREATE KEYSPACE IF NOT EXISTS $keySpace WITH replication = {'class': 'SimpleStrategy', 'replication_factor' : 1};"
  }

  protected[this] def createKeySpace(keySpace: KeySpace): String = createKeySpace(keySpace.name)

}

private[phantom] object CassandraProperties {
  val ZookeeperEnvironmentString: String = "TEST_ZOOKEEPER_CONNECTOR"
}
