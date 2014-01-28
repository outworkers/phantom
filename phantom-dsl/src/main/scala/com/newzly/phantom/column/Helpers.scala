package com.newzly.phantom.column

trait Helpers {
  private[phantom] implicit class RichSeq[T](val l: Seq[T]) {
    final def toOption: Option[Seq[T]] = if (l.isEmpty) None else Some(l)
  }
}
