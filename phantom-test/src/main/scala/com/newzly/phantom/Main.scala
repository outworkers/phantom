package com.newzly.phantom

import org.cassandraunit.utils.EmbeddedCassandraServerHelper

object Main {
  def main(args: Array[String]): Unit = {
    Console.println("Start Cassandra server")
    EmbeddedCassandraServerHelper.startEmbeddedCassandra()
  }
}
