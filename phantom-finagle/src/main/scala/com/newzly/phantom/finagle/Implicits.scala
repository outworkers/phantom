package com.newzly.phantom.finagle

import scala.concurrent.{ ExecutionContext, Future => ScalaFuture }
import com.twitter.util.{ Future, Promise }

object Implicits {
  implicit def transformToTwitterFuture[R](sf: ScalaFuture[R])(implicit executorContext: ExecutionContext): Future[R] = {
      val p = new Promise[R]

      sf.onSuccess {
        case r => p become Future.value(r)
      }

      sf.onFailure {
        case t => p raise t
      }

      p
  }
}
