/*
 * Copyright 2013 websudos ltd.
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
package com.websudos.phantom.server

import com.datastax.driver.core.Session
import com.newzly.util.testing.cassandra.BaseTestHelper

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, blocking}


object CassandraCluster {
  val keySpace: String = s"phantom_scalatra_test_${System.currentTimeMillis()}"

  val cluster = BaseTestHelper.cluster
  implicit lazy val session: Session = blocking {
    cluster.connect()
  }
  implicit lazy val context: ExecutionContext = global

  def createKeySpace(spaceName: String) = {
    blocking {
      session.execute(s"CREATE KEYSPACE IF NOT EXISTS $spaceName WITH replication = {'class': 'SimpleStrategy', 'replication_factor' : 1};")
      session.execute(s"use $spaceName;")
    }
  }
}

trait CassandraCluster {
  implicit lazy val session = blocking {
    CassandraCluster.cluster.connect(CassandraCluster.keySpace)
  }

  def ensureKeyspaceExists() {
    CassandraCluster.createKeySpace(CassandraCluster.keySpace)
  }
}
