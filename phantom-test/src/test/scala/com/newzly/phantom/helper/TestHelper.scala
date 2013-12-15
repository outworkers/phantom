package com.newzly.phantom.helper

import org.cassandraunit.utils.EmbeddedCassandraServerHelper

object TestHelper {

  var clusterInited = false;

  def isClusterInited: Boolean = this.synchronized {
    clusterInited
  }

  def initKeySpaces: Unit = this.synchronized {
    val session = CassandraInst.cassandraSession;
    session.execute("CREATE KEYSPACE testSpace WITH replication = {'class': 'SimpleStrategy', 'replication_factor' : 1};")
    session.execute("use testSpace;")
  }

  def initCluster: Unit = {
    this.synchronized {
      if (isClusterInited == false) {
        EmbeddedCassandraServerHelper.startEmbeddedCassandra()
        clusterInited = true
      }
    }

  }


}
