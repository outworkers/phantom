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

import com.datastax.driver.core.Session

trait CassandraConnector {

  def keySpace: String

  def manager: CassandraManager = DefaultCassandraManager

  implicit def session: Session = manager.session
}


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
    manager.initIfNotInited(keySpace)
    manager.session
  }
}
