package com.newzly.cassandra

import com.datastax.driver.core.{ Cluster }

trait CassandraCluster {

  val service = Cassandra.service

  val cluster =
    Cluster.builder()
      .addContactPoint("127.0.0.1")
      .withPort(9999)
      .withoutJMXReporting()
      .withoutMetrics()
      .build()

  implicit val session = cluster.connect()
}