package com.newzly.phantom.iteratee

import org.scalatest.concurrent.ScalaFutures
import com.newzly.phantom.tables.PrimitivesJoda
import com.newzly.util.testing.AsyncAssertionsHelper._
import java.util.concurrent.atomic.AtomicLong

class IterateeBigReadTest extends BigTest with ScalaFutures {

  val keySpace: String = "BigIterateeTestSpace"

  it should "read the records found in the table" in {
    val counter: AtomicLong = new AtomicLong(0)
    val result = PrimitivesJoda.select.fetchEnumerator  flatMap (_ run Iteratee.forEach {
      r => counter.incrementAndGet()
    })

    result.successful {
      query => {
        info(s"done, reading: ${counter.get}")
        assert(counter.get() === 2000000)
      }
    }
  }
}
