package com.newzly.phantom.iteratee
import play.api.libs.iteratee.{Iteratee => PIteratee, Enumerator => PlayEnumerator}
import scala.concurrent.ExecutionContext

/**
 * Helper object to some commune use cases for iteratee
 * This is a wrapper around play Iteratee class
 */
object Iteratee {
  def collect[R]()(implicit ec: ExecutionContext): PIteratee[R,Seq[R]] =
    PIteratee.fold(Seq.empty[R])((acc, e: R)=> acc :+ e)
  def forEach[E](f: E => Unit)(implicit ec: ExecutionContext) = PIteratee.foreach(f: E => Unit)

}
