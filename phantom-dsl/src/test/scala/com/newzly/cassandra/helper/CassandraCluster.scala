package com.newzly.cassandra.helper

import com.datastax.driver.core.Cluster
import org.apache.cassandra.service.EmbeddedCassandraService
import org.apache.cassandra.io.util.FileUtils
import org.cassandraunit.utils.EmbeddedCassandraServerHelper;
object CassandraInst {
  val embedded = EmbeddedCassandraServerHelper.startEmbeddedCassandra()
  lazy val cluster =  Cluster.builder()
    .addContactPoint("localhost")
    .withPort(9142)
    .withoutJMXReporting()
    .withoutMetrics()
    .build()

  lazy val cassandraSession = cluster.connect()
  def resetCluster() = EmbeddedCassandraServerHelper.cleanEmbeddedCassandra()
}

trait CassandraCluster {
  val embeddedServer = CassandraInst.embedded
  val cluster = CassandraInst.cluster
  val cassandraSession = CassandraInst.cassandraSession
  def resetCluster() = CassandraInst.resetCluster()
}
