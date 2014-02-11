/*
package com.newzly.phantom.iteratee
import scalaz._
import iteratee._
import Iteratee._
import com.datastax.driver.core.{Row, ResultSet}
import scala.collection.JavaConversions._
import scalaz.effect.IO
import com.twitter.util.{Promise, Future}

case class IResultSet(r: ResultSet) {

  implicit val FutureMonad = new Monad[Future] {
    def point[A](a: => A): Future[A] = Future(a)
    def bind[A, B](fa: Future[A])(f: (A) => Future[B]): Future[B] = fa flatMap f
  }

  private val enumerator = enumIterator[Row,IO](r.iterator())

  val result = Future {(consume1[Row, IO]    &= enumerator).run.unsafePerformIO()}

  def mapResult(x: Row => Unit)  = {
    def f: IterateeT[Row, IO, Future[Unit]] = consume2[Row, Future](x)
    (f &= enumerator).run.unsafePerformIO()
  }

  def consume1[E, F[_]:Monad]: IterateeT[E, F, List[E]] = {
    def init = List.empty[E]
    def write(acc: List[E], e: E) = IO(acc :+ (e)).asInstanceOf[F[List[E]]]
    foldM[E, F, List[E]](init) { (acc: List[E], e: E) =>
      write(acc,e)
    }
  }

  def consume2[E, F[Unit]:Monad](f: E => Unit): IterateeT[E, IO, Future[Unit]] = {
    def write(a: Future[Unit], e: E) = IO(a flatMap(_=>Future(f(e))))
    foldM(Future.Done) ((a: Future[Unit], e: E) => write(a,e))
  }
}*/
