package com.newzly.phantom.helper

import org.cassandraunit.utils.EmbeddedCassandraServerHelper
import com.newzly.phantom.{PrimitiveColumn, CassandraTable}
import com.datastax.driver.core.{Session, Row}
import scala.concurrent.{Await, Future}
import scala.concurrent.duration._
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

  }
  def initialized = {
    isInitialized.get()
  }

  def externalInitCluster() = {
    externalShutDownCluster()
    initKeySpaces
    initTables
    isInitialized.set(true)
  }

  def externalShutDownCluster() = {
    try {
      session.execute("DROP KEYSPACE testSpace;")
    } catch {
      case _ => println ("Exception caught")
    }
  }

}
