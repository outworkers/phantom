package com.newzly.phantom.dsl

import org.scalatest.FlatSpec
import com.newzly.phantom.tables.{ Articles, Primitives }

class FieldCollectionTest extends FlatSpec {

  it should "correctly reference the same table" in {
     assert(Primitives.pkey.getTable eq Primitives)
  }


  it should "not init fields names by default" in {
    assert(Articles.columns.length === 3)
  }
}
