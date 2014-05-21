package com.newzly.phantom.server

import scala.concurrent.{ blocking, ExecutionContext }
import scala.concurrent.ExecutionContext.global
import com.datastax.driver.core.Session
import com.newzly.util.testing.cassandra.BaseTestHelper

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