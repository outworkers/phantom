package com.newzly.phantom

import com.datastax.driver.core.Cluster
import org.apache.cassandra.service.EmbeddedCassandraService
import org.apache.cassandra.io.util.FileUtils
import org.cassandraunit.utils.EmbeddedCassandraServerHelper;
object CassandraInst {

  //lazy
  /*val service1 = {
    try {
    System.setProperty("cassandra.config", getClass.getClassLoader.getResource("cassandra.yaml").toString)
    System.setProperty("cassandra-foreground", "yes")
    System.setProperty("log4j.defaultInitOverride", "false")
    val f = new java.io.File("tmp")
    if (f.exists()) FileUtils.deleteRecursive(f)
    f.mkdir()
    println (f.getAbsolutePath)
    val s = new EmbeddedCassandraService
    s.start()
   // s
    } catch {
      case e: Exception =>
        Nil
    }
  }*/
  val s = {
    println("======== start embedding cassandra server ===========")
    val f = new java.io.File("tmp")
    if (f.exists()) FileUtils.deleteRecursive(f)
    f.mkdir()
    EmbeddedCassandraServerHelper.startEmbeddedCassandra()//EmbeddedCassandraServerHelper.DEFAULT_CASSANDRA_YML_FILE,f.getAbsolutePath)
  }
}

trait CassandraCluster {

  val embeddedServer = CassandraInst
  lazy val cluster =
    Cluster.builder()
      .addContactPoint("localhost")
      .withPort(9142)
      .withoutJMXReporting()
      .withoutMetrics()
      .build()
  //.withPort(9042)
  lazy val cassandraSession = cluster.connect()
}
