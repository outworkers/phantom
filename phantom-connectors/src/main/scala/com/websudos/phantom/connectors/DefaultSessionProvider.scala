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

import com.datastax.driver.core.{Cluster, Session}

import scala.concurrent.blocking
import scala.util.control.NonFatal
import scala.util.{Failure, Success, Try}

/**
 * The default SessionProvider implementation, which should be sufficient
 * for the most use cases.
 *
 * This implementation caches `Session` instances per keySpace.
 */
class DefaultSessionProvider(
  val space: KeySpace,
  builder: ClusterBuilder,
  errorHandler: Throwable => Throwable = identity
) extends SessionProvider {

  val cluster: Cluster = {
    builder(Cluster.builder).withoutJMXReporting().withoutMetrics().build
  }

  /**
   * Initializes the keySpace with the given name on
   * the specified Session.
   */
  protected[this] def initKeySpace(session: Session, keySpace: String): Session = blocking {
    blocking {
      session.execute(s"CREATE KEYSPACE IF NOT EXISTS $keySpace WITH replication = {'class': 'SimpleStrategy', 'replication_factor' : 1};")
    }

    session
  }

  /**
   * Creates a new Session for the specified keySpace.
   */
  protected[this] def createSession(keySpace: String): Session = {
    Try {
        val session = blocking {
          cluster.connect
        }

        initKeySpace(session, keySpace)
    } match {
      case Success(value) => value
      case Failure(NonFatal(err)) => throw errorHandler(err);
    }
  }

  val session = createSession(space.name)
}
