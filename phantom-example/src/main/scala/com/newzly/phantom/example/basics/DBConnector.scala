package com.newzly.phantom.example.basics

import scala.concurrent. { blocking, Future }
import com.datastax.driver.core.{ Cluster, Session }
import com.newzly.phantom.Implicits._

object DBConnector {
  val keySpace = "phantom_examples"

  lazy val cluster =  Cluster.builder()
    .addContactPoint("localhost")
    .withPort(9042)
    .withoutJMXReporting()
    .withoutMetrics()
    .build()

  lazy val session = blocking {
    cluster.connect(keySpace)
  }
}

trait DBConnector {
  self: CassandraTable[_, _] =>

  def createTable(): Future[Unit] ={
    create.future() map (_ => ())
  }

  implicit lazy val datastax: Session = DBConnector.session
}