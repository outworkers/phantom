package com.newzly.phantom.cassandra


object CassandraTestHelper {
  def main(args: Array[String]) {
    Console.println("start Cassandra server")
  }
  EmbeddedCassandraServerHelper.startEmbeddedCassandra()
}