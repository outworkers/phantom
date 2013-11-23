package com.newzly.cassandra

import com.datastax.driver.core.{ Cluster }

trait CassandraCluster {

  val service = Cassandra.service

  val cluster =
    Cluster.builder()
      .addContactPoint("127.0.0.1")
      .withPort(19042)
      .withoutJMXReporting()
      .withoutMetrics()
      .build()

  implicit val session = cluster.connect()
}