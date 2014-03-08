package com.newzly.phantom.dsl

import org.scalatest.FlatSpec
import com.newzly.phantom.tables.{ Articles, Primitives }

class FieldCollectionTest extends FlatSpec {

  it should "collect objects in the same order they are written" in {
    Console.println(Primitives.columns.map(_.name).mkString(" "))
  }


  it should "correctly reference the same table" in {
     assert(Primitives.pkey.table eq Primitives)
  }


  it should "not init fields names by default" in {
    assert(Articles.columns.length === 3)
  }
}
