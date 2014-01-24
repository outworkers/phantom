package com.newzly.phantom.helper

import org.scalatest.Assertions
import org.scalatest._
import org.scalatest.concurrent.{PatienceConfiguration, AsyncAssertions, ScalaFutures}
import com.twitter.util.Future
import org.scalatest.time.{ Millis, Seconds, Span }

import org.scalatest.time.SpanSugar._
import scala.collection.concurrent.TrieMap

object AsyncAssertionsHelper extends ScalaFutures {

  implicit val s: PatienceConfiguration.Timeout = timeout(1 second)

  implicit class Failing[A](val f: Future[A]) extends Assertions with AsyncAssertions {


    def failing[T  <: Throwable](implicit mf: Manifest[T], timeout: PatienceConfiguration.Timeout) = {
      val w = new Waiter

      f onSuccess  {
        res => w.dismiss()
      }

      f onFailure {
        e => w(throw e); w.dismiss()
      }
      intercept[T] {
        w.await(timeout, dismissals(1))
      }
    }

    def successful(x: A => Unit)(implicit timeout: PatienceConfiguration.Timeout) = {
      val w = new Waiter

      f onSuccess {
        case res => w{x(res)}; w.dismiss()
      }

      f onFailure {
        e => w(throw e); w.dismiss()
      }
      Console.println(s"Timeout ${timeout.value.toMillis}")
      w.await(timeout, dismissals(1))
    }
  }


}