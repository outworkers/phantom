package net.liftweb
import com.datastax.driver.core.querybuilder._

package object cassandra {
  trait Helpers {
    private[cassandra] implicit class RichSeq[T](val l: Seq[T]) {
      final def toOption: Option[Seq[T]] = if (l.isEmpty) None else Some(l)
    }
  }

  type JMap[K, V] = java.util.Map[K, V];

  type QueryBuilder = com.datastax.driver.core.querybuilder.QueryBuilder
}