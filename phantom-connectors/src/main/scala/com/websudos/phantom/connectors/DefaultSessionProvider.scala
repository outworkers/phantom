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

import scala.collection.concurrent.TrieMap

/**
 * The default SessionProvider implementation, which should be sufficient
 * for the most use cases.
 *
 * This implementation caches `Session` instances per keySpace.
 */
class DefaultSessionProvider(builder: ClusterBuilder) extends SessionProvider {

  private val sessionCache = new Cache[String, Session]


  lazy val cluster: Cluster = {
    // TODO - the original phantom modules had .withoutJMXReporting().withoutMetrics() as defaults, discuss best choices
    val cb = Cluster
      .builder
    builder(cb).build
  }

  /**
   * Initializes the keySpace with the given name on
   * the specified Session.
   */
  protected def initKeySpace(session: Session, keySpace: String): Session = {
    session.execute(s"CREATE KEYSPACE IF NOT EXISTS $keySpace WITH replication = {'class': 'SimpleStrategy', 'replication_factor' : 1};")
    session
  }

  /**
   * Creates a new Session for the specified keySpace.
   */
  protected[this] def createSession(keySpace: String): Session = {
    val session = cluster.connect
    initKeySpace(session, keySpace)
  }

  def getSession(keySpace: String): Session = {
    sessionCache.getOrElseUpdate(keySpace, createSession(keySpace))
  }

}

/**
 * Thread-safe cache implementation.
 *
 * Given the expected use cases (a map with often just one or at most
 * a handful of elements in it and being accessed infrequently), this
 * implementation is not aggressively optimized and focusses on thread-safety.
 */
class Cache[K, V] {

  /* this implementation uses putIfAbsent from the underlying TrieMap as
   * getOrElseUpdate is not thread-safe. */

  private[this] val map = TrieMap[K, Lazy]()

  private[this] class Lazy(value: => V) {
    lazy val get: V = value
  }

  /**
   * Get the element for the specified key
   * if it has already been set or otherwise
   * associate the key with the given (lazy) value.
   *
   * @return the value previously associated with the key
   * or (if no value had been previously set) the specified new value.
   */
  def getOrElseUpdate(key: K, op: => V): V = {
    val lazyOp = new Lazy(op)
    map.putIfAbsent(key, lazyOp) match {
      case Some(oldval) =>
        // don't evaluate the new lazyOp, return existing value
        oldval.get
      case _ =>
        // no existing value for key, evaluate lazyOp
        lazyOp.get
    }
  }

}