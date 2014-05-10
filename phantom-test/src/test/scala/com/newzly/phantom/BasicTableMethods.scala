package com.newzly.phantom

import org.scalatest.{ FlatSpec, Matchers, ParallelTestExecution }
import org.scalatest.prop.GeneratorDrivenPropertyChecks
import com.newzly.phantom.tables.{ BasicTable, ComplexCompoundKeyTable, SimpleCompoundKeyTable }

class BasicTableMethods extends FlatSpec with Matchers with GeneratorDrivenPropertyChecks with ParallelTestExecution {

  it should "retrieve the correct number of columns in a simple table" in {
    forAll(minSuccessful(300)) { (d: String) =>
      whenever (d.length > 0) {
        BasicTable.columns.length shouldEqual 4
      }
    }
  }

  it should "retrieve the correct number of columns in a big table" in {
    forAll(minSuccessful(300)) { (d: String) =>
      whenever (d.length > 0) {
        ComplexCompoundKeyTable.columns.length shouldEqual 10
      }
    }
  }

  it should "retrieve the correct number of primary keys for a table" in {
    forAll(minSuccessful(300)) { (d: String) =>
      whenever (d.length > 0) {
        SimpleCompoundKeyTable.primaryKeys.length shouldEqual 2
        SimpleCompoundKeyTable.partitionKeys.length shouldEqual 1
      }
    }
  }
}
