package com.websudos.phantom.dsl

import org.scalatest.{ FlatSpec, Matchers, ParallelTestExecution }
import org.scalatest.prop.GeneratorDrivenPropertyChecks
import com.websudos.phantom.tables.{ Articles, Primitives }

class FieldCollectionTest extends FlatSpec with Matchers with ParallelTestExecution with GeneratorDrivenPropertyChecks {

  it should "corrrectly initialise objects in the order they are written in" in {
    forAll(minSuccessful(300)) { (d: String) =>
      whenever (d.length > 0) {
        val collected = Articles.columns.map(_.name).mkString(" ")
        val expected = s"${Articles.orderId.name} ${Articles.id.name} ${Articles.name.name}"
        collected shouldEqual expected
      }
    }
  }

  it should "collect objects in the same order they are written" in {
    val collected = Articles.columns.map(_.name).mkString(" ")
    val expected = s"${Articles.orderId.name} ${Articles.id.name} ${Articles.name.name}"
    collected shouldEqual expected
  }


  it should "correctly reference the same table" in {
     Primitives.pkey.table shouldEqual Primitives
  }


  it should "initialise fields by default" in {
    Articles.columns.length shouldEqual 3
  }
}
