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
 * - Explicit consent must be obtained from the copyright owner, Outworkers Limited before any redistribution is made.
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
package com.websudos.phantom.connectors

import scala.collection.JavaConverters._
import com.datastax.driver.core.{ProtocolVersion, Session}

import scala.util.control.NoStackTrace

trait SessionAugmenter {

  def session: Session

  def protocolVersion: ProtocolVersion = {
    session.getCluster.getConfiguration.getProtocolOptions.getProtocolVersion
  }

  def isNewerThan(pv: ProtocolVersion): Boolean = {
    protocolVersion.compareTo(pv) > 0
  }

  def v3orNewer : Boolean = isNewerThan(ProtocolVersion.V2)

  def protocolConsistency: Boolean = isNewerThan(ProtocolVersion.V1)

  def v4orNewer : Boolean = isNewerThan(ProtocolVersion.V3)
}

trait SessionAugmenterImplicits {
  implicit class RichSession(val session: Session) extends SessionAugmenter
}

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
class KeySpaceDef(
  val name: String,
  clusterBuilder: ClusterBuilder,
  autoinit: Boolean,
  keyspaceFn: Option[(Session, KeySpace) => String] = None
) { outer ⇒

  lazy val provider: SessionProvider = new DefaultSessionProvider(KeySpace(name), clusterBuilder, autoinit, keyspaceFn)

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
  trait Connector extends com.websudos.phantom.connectors.Connector with SessionAugmenterImplicits {

    lazy val provider = outer.provider

    lazy val keySpace = outer.name

    implicit val space: KeySpace = KeySpace(outer.name)

    def cassandraVersion: Option[VersionNumber] = outer.cassandraVersion

    def cassandraVersions: Set[VersionNumber] = outer.cassandraVersions
  }

}

case class KeySpace(name: String)
