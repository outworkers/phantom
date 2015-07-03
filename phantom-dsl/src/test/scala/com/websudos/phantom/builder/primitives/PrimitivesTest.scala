package com.websudos.phantom.builder.primitives

import java.nio.ByteBuffer

import org.joda.time.DateTime
import org.scalatest.{FlatSpec, Matchers}

class PrimitivesTest extends FlatSpec with Matchers {

  it should "coerce a DateTime into a valid timezone string" in {
    val date = new DateTime(2014, 6, 2, 10, 5)

    DateSerializer.asCql(date) shouldEqual date.getMillis.toString
  }

  it should "convert ByteBuffers to valid hex bytes" in {
    val buf = ByteBuffer.wrap(Array[Byte](1, 2, 3, 4, 5))
    Primitive[ByteBuffer].asCql(buf) shouldEqual "0x0102030405"

    buf.position(2)   // Non-zero position
    Primitive[ByteBuffer].asCql(buf) shouldEqual "0x030405"

    val slice = buf.slice()   // Slice with non-zero arrayOffset
    Primitive[ByteBuffer].asCql(slice) shouldEqual "0x030405"
  }

}
