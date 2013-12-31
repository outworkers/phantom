package com.newzly.phantom.helper

import com.datastax.driver.core.Cluster
import org.apache.cassandra.service.EmbeddedCassandraService
import org.apache.cassandra.io.util.FileUtils
import org.cassandraunit.utils.EmbeddedCassandraServerHelper;

object CassandraInst {

  var inited = false

  def isInited: Boolean = this.synchronized {
    inited
  }

  def init() : Unit = {
    this.synchronized {
      try {
      if (!isInited) {
        inited = true
        EmbeddedCassandraServerHelper.startEmbeddedCassandra()
      }
      } catch {

        case e: Throwable => {
          EmbeddedCassandraServerHelper.cleanEmbeddedCassandra();
          EmbeddedCassandraServerHelper.startEmbeddedCassandra()
          inited = true
        }
      }
    }
  }

  init()

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
  val cluster = CassandraInst.cluster
  val cassandraSession = CassandraInst.cassandraSession
  def resetCluster() = CassandraInst.resetCluster()
}
