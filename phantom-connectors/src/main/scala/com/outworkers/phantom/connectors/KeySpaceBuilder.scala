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
package com.outworkers.phantom.connectors

import com.datastax.driver.core.PoolingOptions

/**
 * A builder for KeySpace instances.
 *
 * When using multiple keySpaces in the same Cassandra cluster,
 * it is recommended to create all `KeySpace` instances from the
 * same builder instance.
 */
class KeySpaceBuilder(clusterBuilder: ClusterBuilder) {

  /**
   * Specify an additional builder to be applied when creating the Cluster instance.
   * This hook exposes the underlying Java API of the builder API of the Cassandra
   * driver.
   */
  def withClusterBuilder(builder: ClusterBuilder): KeySpaceBuilder =
    new KeySpaceBuilder(clusterBuilder andThen builder)

  /**
    * Disables the heartbeat for the current builder.
    * This is designed for local instantiations of connectors or test environments.
    * @return A new cluster builder, with the heartbeat interval set to 0(disabled).
    */
  def noHeartbeat(): KeySpaceBuilder = {
    new KeySpaceBuilder(clusterBuilder andThen (
      _.withPoolingOptions(new PoolingOptions().setHeartbeatIntervalSeconds(0)))
    )
  }

  /**
    * Creates and can initialise a keyspace with the given name.
    * @param name The name of the keyspace, case sensititve by default.
    * @param autoinit Whether or not to automatically initialise the keyspace before the session is created.
    * @param query The builder to use when producing the keyspace query.
    * @return
    */
  def keySpace(
    name: String,
    autoinit: Boolean = true,
    query: Option[KeySpaceCQLQuery] = None,
    errorHandler: Throwable => Throwable = identity
  ): CassandraConnection = {
    new CassandraConnection(name, clusterBuilder, autoinit, query, errorHandler)
  }

  /**
    * Creates and can initialise a keyspace with the given name.
    * This will automatically initialise the keyspace by default, as we consider
    * passing a specific keyspace query indicates clear intent you want this to happen.
    * @param name The name of the keyspace, case sensititve by default.
    * @param query The builder to use when producing the keyspace query.
    * @return
    */
  @deprecated("Simply pass in a keySpace query, the keyspace is not required", "2.8.5")
  def keySpace(
    name: String,
    query: KeySpaceCQLQuery
  ): CassandraConnection = {
    new CassandraConnection(name, clusterBuilder, true, Some(query))
  }

  /**
    * Creates and can initialise a keyspace with the given name.
    * This will automatically initialise the keyspace by default, as we consider
    * passing a specific keyspace query indicates clear intent you want this to happen.
    * @param query The builder to use when producing the keyspace query.
    * @return
    */
  def keySpace(
    query: KeySpaceCQLQuery
  ): CassandraConnection = {
    new CassandraConnection(query.keyspace, clusterBuilder, true, Some(query))
  }
}

/**
  * This exists to prevent a dependency on the diesel engine
  * or any kind of specific query implementation from within the connectors framework.
  * This allows connectors to be used in isolation from the rest of phantom DSL.
  */
trait KeySpaceCQLQuery {
  def keyspace: String = ""

  def queryString: String
}
