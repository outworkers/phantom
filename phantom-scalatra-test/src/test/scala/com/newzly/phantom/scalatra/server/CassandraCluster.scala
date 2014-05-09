package com.newzly.phantom.scalatra.server

import com.datastax.driver.core.Session
import com.newzly.util.testing.cassandra.BaseTestHelper
import scala.concurrent.ExecutionContext
import scala.concurrent.ExecutionContext.global

object CassandraCluster {
  val keySpace: String = s"phantom_scalatra_test_${System.currentTimeMillis()}"

  val cluster = BaseTestHelper.cluster
  implicit lazy val session: Session = cluster.connect()
  implicit lazy val context: ExecutionContext = global

  def createKeySpace(spaceName: String) = {
    session.execute(s"CREATE KEYSPACE IF NOT EXISTS $spaceName WITH replication = {'class': 'SimpleStrategy', 'replication_factor' : 1};")
    session.execute(s"use $spaceName;")
  }
}

trait CassandraCluster {
  implicit lazy val session =
    CassandraCluster.cluster.connect(CassandraCluster.keySpace)

  def ensureKeyspaceExists() {
    CassandraCluster.createKeySpace(CassandraCluster.keySpace)
  }
}