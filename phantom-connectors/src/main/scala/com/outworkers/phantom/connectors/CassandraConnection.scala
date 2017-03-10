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

import com.datastax.driver.core.Session

import scala.collection.JavaConverters._
import scala.util.control.NoStackTrace

/**
 * Represents a single Cassandra keySpace.
 *
 * Provides access to the associated `Session` as well as to a
 * `Connector` trait that can be mixed into `CassandraTable`
 * instances.
 *
 * @param name the name of the keySpace
 * @param clusterBuilder the provider for this keySpace
 */
class CassandraConnection(
  val name: String,
  clusterBuilder: ClusterBuilder,
  autoinit: Boolean,
  keyspaceFn: Option[KeySpaceCQLQuery] = None,
  errorHander: Throwable => Throwable = identity
) { outer =>

  lazy val provider = new DefaultSessionProvider(
    KeySpace(name),
    clusterBuilder,
    autoinit,
    keyspaceFn,
    errorHander
  )

  /**
   * The Session associated with this keySpace.
   */
  lazy val session: Session = provider.session

  def cassandraVersions: Set[VersionNumber] = {
    session.getCluster.getMetadata.getAllHosts
      .asScala.map(_.getCassandraVersion)
      .toSet[VersionNumber]
  }

  def cassandraVersion: Option[VersionNumber] = {
    val versions = cassandraVersions

    if (versions.nonEmpty) {

      val single = versions.headOption

      if (cassandraVersions.size == 1) {
        single
      } else {

        if (single.forall(item => versions.forall(item ==))) {
          single
        } else {
          throw new RuntimeException(
            s"Illegal single version comparison. You are connected to clusters of different versions." +
              s"Available versions are: ${versions.mkString(", ")}"
          ) with NoStackTrace
        }
      }
    } else {
      throw new RuntimeException("Could not extract any versions from the cluster, versions were empty")
    }
  }

  /**
   * Trait that can be mixed into `CassandraTable`
   * instances.
   */
  trait Connector extends com.outworkers.phantom.connectors.Connector with SessionAugmenterImplicits {

    lazy val provider: DefaultSessionProvider = outer.provider

    lazy val keySpace: String = outer.name

    implicit val space: KeySpace = KeySpace(outer.name)

    def cassandraVersion: Option[VersionNumber] = outer.cassandraVersion

    def cassandraVersions: Set[VersionNumber] = outer.cassandraVersions
  }

}
