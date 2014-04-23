package com.newzly.phantom.helper

import com.datastax.driver.core.Cluster

object BaseTestHelper {
  val cluster = Cluster.builder()
    .addContactPoint("localhost")
    .withPort(9142)
    .withoutJMXReporting()
    .withoutMetrics()
    .build()

}

