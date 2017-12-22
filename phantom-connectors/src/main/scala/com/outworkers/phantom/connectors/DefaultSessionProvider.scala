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
  autoinit: Boolean = true,
  keyspaceQuery: Option[KeySpaceCQLQuery] = None,
  errorHandler: Throwable => Throwable = identity
) extends SessionProvider {

  val cluster: Cluster = builder(Cluster.builder).build

  /**
   * Initializes the keySpace with the given name on
   * the specified Session.
   */
  protected[this] def initKeySpace(session: Session, space: String): Session = blocking {
    blocking {
      val query = keyspaceQuery.map(_.queryString).getOrElse(defaultKeyspaceCreationQuery(space))
      logger.info(s"Automatically initialising keyspace $space with query $query")
      session.execute(query)
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

      if (autoinit) {
        initKeySpace(session, keySpace)
      } else {
        logger.info(s"Auto-init set to false, keyspace $space is not being auto-created.")
        session
      }
    } match {
      case Success(value) => value
      case Failure(NonFatal(err)) => throw errorHandler(err)
    }
  }

  lazy val session: Session = createSession(space.name)
}
