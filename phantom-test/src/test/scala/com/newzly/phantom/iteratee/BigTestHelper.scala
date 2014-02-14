package com.newzly.phantom.iteratee

import com.datastax.driver.core.Cluster

object BigTestHelper {

  val cluster = Cluster.builder()
    .addContactPoint("localhost")
    .withPort(9042)
    .withoutJMXReporting()
    .withoutMetrics()
    .build()

}
