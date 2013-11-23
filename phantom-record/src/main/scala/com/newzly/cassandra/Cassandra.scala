package com.newzly.cassandra

import org.apache.cassandra.io.util.FileUtils

object Cassandra {

  lazy val service: EmbeddedCassandraService = {
    System.setProperty("cassandra.config",
      getClass.getClassLoader.getResource("cassandra.yaml").toString
    )
    System.setProperty("cassandra-foreground", "yes")
    System.setProperty("log4j.defaultInitOverride", "false")
    FileUtils.deleteRecursive(new java.io.File("tmp"))
    val s = new EmbeddedCassandraService
    s.start()
    s
  }

}