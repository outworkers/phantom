package com.newzly.phantom.helper

import org.cassandraunit.utils.EmbeddedCassandraServerHelper
import com.newzly.phantom.{PrimitiveColumn, CassandraTable}
import com.datastax.driver.core.{Session, Row}
import java.util.concurrent.atomic.AtomicBoolean

object TestHelper extends Tables{
  implicit val session: Session = CassandraInst.cassandraSession
  private val isInitialized =  new AtomicBoolean(false)
  private[this] def initKeySpaces() = {
    session.execute("CREATE KEYSPACE testSpace WITH replication = {'class': 'SimpleStrategy', 'replication_factor' : 1};")
    session.execute("use testSpace;")
  }

  private[this] def initTables() = {
    createTables
  }

  def initCluster() = {
    initKeySpaces
    initTables
    isInitialized.set(true)
  }
  def initialized = {
    isInitialized.get()
  }

}
