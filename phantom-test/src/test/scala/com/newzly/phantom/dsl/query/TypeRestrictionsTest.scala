package com.newzly.phantom.dsl.query

import org.scalatest.{ FlatSpec, Matchers, ParallelTestExecution }
import com.newzly.phantom.Implicits._
import com.newzly.phantom.tables.Primitives

class TypeRestrictionsTest extends FlatSpec with Matchers {
  it should "allow using a correct type for a value method" in {
    "Primitives.insert.value(_.boolean, true)" should compile
  }

  it should "not allow using a wrong type for a value method" in {
    "Primitives.insert.value(_.boolean, 5)" shouldNot compile
  }
}
