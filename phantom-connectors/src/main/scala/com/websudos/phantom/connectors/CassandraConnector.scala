/*
 *
 *  * Copyright 2015 websudos ltd.
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

package com.websudos.phantom.connectors

import com.datastax.driver.core.Session

private[connectors] case object CassandraInitLock

class EmptyClusterStoreException extends RuntimeException("Attempting to retrieve Cassandra cluster reference before initialisation")

class EmptyPortListException extends RuntimeException("Cannot build a cluster from an empty list of addresses")

trait CassandraConnector {

  implicit def keySpace: KeySpace

  def manager: CassandraManager = DefaultCassandraManager

  implicit def session: Session = manager.session
}
