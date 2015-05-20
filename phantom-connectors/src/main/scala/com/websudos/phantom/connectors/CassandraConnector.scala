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

import com.datastax.driver.core.{VersionNumber, Session}

private[connectors] case object CassandraInitLock

class EmptyClusterStoreException extends RuntimeException("Attempting to retrieve Cassandra cluster reference before initialisation")

class EmptyPortListException extends RuntimeException("Cannot build a cluster from an empty list of addresses")


sealed trait VersionBuilder {
  def apply(major: Int, minor: Int, patch: Int): VersionNumber = {
    VersionNumber.parse(s"$major.$minor.$patch")
  }
}

/**
 * The root implementation of a Cassandra connection.
 * By default, the in phantom-connectors framework the only 2 primitives needed for connection are the KeySpace and the manager.
 */
trait CassandraConnector {

  implicit def keySpace: KeySpace

  val manager: CassandraManager = DefaultCassandraManager

  implicit def session: Session = {
    manager.initIfNotInited(keySpace.name)
    manager.session
  }

  def cassandraVersions: Set[VersionNumber] = {
    manager.cassandraVersions
  }

  def cassandraVersion: VersionNumber = {
    val single = manager.cassandraVersions.head

    if (manager.cassandraVersions.size == 1) {
      single
    } else {
      if (manager.cassandraVersions.forall(_.compareTo(single) == 0)) {
        single
      } else {
        throw new Exception("Illegal single version comparison. You are connected to clusters of different versions")
      }
    }

  }

  object Version extends VersionBuilder {
    val `2.0.8` = apply(2, 0, 8)
    val `2.0.13` = apply(2, 0, 13)
    val `2.1.0` = apply(2, 1, 0)
  }
}
