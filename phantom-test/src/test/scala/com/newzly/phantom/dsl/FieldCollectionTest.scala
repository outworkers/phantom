package com.newzly.phantom.dsl

import org.scalatest.FlatSpec
import com.newzly.phantom.tables.Primitives

class FieldCollectionTest extends FlatSpec {

  it should "correctly print all field names" in {
    Console.println(Primitives.columns.length)
    Primitives.columns.foreach {
      column => {
        Console.println(column.name)
      }
    }

    assert(Primitives.columns.length === 11)
  }
}
