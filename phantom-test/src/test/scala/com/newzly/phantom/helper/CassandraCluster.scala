package com.newzly.phantom.helper

import com.datastax.driver.core.Cluster
import org.apache.cassandra.service.EmbeddedCassandraService
import org.apache.cassandra.io.util.FileUtils
import org.cassandraunit.utils.EmbeddedCassandraServerHelper
import com.datastax.driver.core.policies.{ConstantReconnectionPolicy, DowngradingConsistencyRetryPolicy}
;

object CassandraInst {

  var inited = false

  def isInited: Boolean = this.synchronized {
    inited
  }

  lazy val cluster =  Cluster.builder()
    .addContactPoint("54.241.18.80")
    .withPort(9042)
    .withRetryPolicy(DowngradingConsistencyRetryPolicy.INSTANCE)
    .withReconnectionPolicy(new ConstantReconnectionPolicy(100L))
    .build()

  lazy val cassandraSession = cluster.connect()

}

trait CassandraCluster {
  val cluster = CassandraInst.cluster
  val cassandraSession = CassandraInst.cassandraSession

}
