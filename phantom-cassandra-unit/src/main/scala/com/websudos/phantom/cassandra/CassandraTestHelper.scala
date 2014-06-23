package com.websudos.phantom.cassandra

import org.cassandraunit.utils.EmbeddedCassandraServerHelper

object CassandraTestHelper {
   def main(args: Array[String]) {
     Console.println("start Cassandra server")
   }
   EmbeddedCassandraServerHelper.startEmbeddedCassandra()
 }
