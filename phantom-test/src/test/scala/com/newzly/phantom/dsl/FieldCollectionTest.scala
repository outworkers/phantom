package com.newzly.phantom.dsl

import org.scalatest.{ FlatSpec, Matchers }
import com.newzly.phantom.tables.{ Articles, Primitives }

class FieldCollectionTest extends FlatSpec with Matchers {

  it should "collect objects in the same order they are written" in {
    val collected = Articles.columns.map(_.name).mkString(" ")
    val expected = s"${Articles.order_id.name} ${Articles.id.name} ${Articles.name.name}"
    collected shouldBe expected
  }


  it should "correctly reference the same table" in {
     assert(Primitives.pkey.table eq Primitives)
  }


  it should "initialise fields by default" in {
    assert(Articles.columns.length === 3)
  }
}
