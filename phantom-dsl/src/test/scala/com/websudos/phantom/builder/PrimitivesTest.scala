package com.websudos.phantom.builder

import com.websudos.phantom.builder.primitives.DateSerializer
import org.joda.time.DateTime
import org.scalatest.{FlatSpec, Matchers}

class PrimitivesTest extends FlatSpec with Matchers {

  it should "coerce a DateTime into a valid timezone string" in {
    val date = new DateTime(2014, 6, 2, 10, 5)

    DateSerializer.asCql(date) shouldEqual "2014-05-02T09:05:00+0000Z"

  }

}
