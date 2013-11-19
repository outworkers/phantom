package net.liftweb.cassandra

package object blackpepper {
  trait Helpers {
    implicit class RichSeq[T](val l: Seq[T]) {
      final def toOption: Option[Seq[T]] = if (l.isEmpty) None else Some(l)
    }
  }

  type JMap[K, V] = java.util.Map[K, V];
}