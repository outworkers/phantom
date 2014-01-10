package com.newzly.phantom.helper

import org.scalatest.Assertions
import org.scalatest.concurrent.AsyncAssertions
import com.twitter.util.Future
import org.scalatest.time.{Millis, Span}

object AsyncAssertionsHelper {

  implicit class Failing[A](val f: Future[A]) extends Assertions with AsyncAssertions {

    def failing[T  <: Throwable : Manifest] = {
      val w = new Waiter

      f onSuccess  {
        res => w.dismiss()
      }

      f onFailure {
        e => w(throw e); w.dismiss()
      }
      intercept[T] {
        w.await
      }
    }

    def successful(x: A => Unit) = {
      val w = new Waiter

      f onSuccess {
        case res => x(res); w.dismiss()
      }

      f onFailure {
        e => w(throw e); w.dismiss()
      }

      w.await()
    }
  }


}