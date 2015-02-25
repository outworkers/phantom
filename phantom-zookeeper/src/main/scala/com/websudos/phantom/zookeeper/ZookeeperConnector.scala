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
package com.websudos.phantom.zookeeper

import com.datastax.driver.core.Session
import com.websudos.phantom.connectors.CassandraConnector

private[zookeeper] case object CassandraInitLock

/**
 * The base implementation of a ZooKeeper connector.
 * By default it needs a ZooKeeper manager to handle spawning a ZooKeeper client and fetching ports from Cassandra.
 * Next it will simply forward the Cassandra session as an implicit while the ZooKeeper Manager does the rest of the magic.
 *
 * The keySpace must also be specified, as the Cassandra connection can only execute queries on a defined keySpace.
 */
trait ZookeeperConnector extends CassandraConnector {

  def zkPath = "/cassandra"
}

/**
 * This is a default implementation of ZooKeeper connector.
 * It will use and initialise the default manager, the session will be forwarded for queries to execute with the proper implicit session.
 */
trait DefaultZookeeperConnector extends ZookeeperConnector {

  override val manager = DefaultZookeeperManagers.defaultManager

  override implicit lazy val session: Session = {
    manager.initIfNotInited(keySpace.name)
    manager.session
  }
}
