package com.newzly
import com.datastax.driver.core.querybuilder._
import org.apache.cassandra.service.EmbeddedCassandraService

package object cassandra {
  trait Helpers {
    private[cassandra] implicit class RichSeq[T](val l: Seq[T]) {
      final def toOption: Option[Seq[T]] = if (l.isEmpty) None else Some(l)
    }
  }

  type JMap[K, V] = java.util.Map[K, V];

  /**
   * Cassandra
   */
  type EmbeddedCassandraService = org.apache.cassandra.service.EmbeddedCassandraService

  /**
   * Datastax imports
   */
  type Session = com.datastax.driver.core.Session;
  type QueryBuilder = com.datastax.driver.core.querybuilder.QueryBuilder
}