package com.newzly.cassandra.phantom.dsl

import org.scalatest.{BeforeAndAfterEach, BeforeAndAfterAll, FlatSpec}
import com.newzly.cassandra.helper.CassandraCluster



class BaseTest  extends FlatSpec  with BeforeAndAfterEach with BeforeAndAfterAll with CassandraCluster {

  override def beforeAll() {//reset cassandra
    //scala.util.Try(cassandraSession.execute("DROP KEYSPACE testspace;"))
    //cassandraSession.execute("CREATE KEYSPACE testSpace WITH replication = {'class': 'SimpleStrategy', 'replication_factor' : 1};")
    //cassandraSession.execute("use testSpace;")
  }
  override def beforeEach() {//reset cassandra
    resetCluster()
    cassandraSession.execute("CREATE KEYSPACE testSpace WITH replication = {'class': 'SimpleStrategy', 'replication_factor' : 1};")
    cassandraSession.execute("use testSpace;")
  }

  override def afterAll() {
    cluster.shutdown()
  }

}
