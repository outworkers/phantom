package com.newzly.phantom

import org.scalatest.{ FlatSpec, Matchers, ParallelTestExecution }
import com.newzly.phantom.tables.{ BasicTable, SimpleCompoundKeyTable, ComplexCompoundKeyTable }

class BasicTableMethods extends FlatSpec with Matchers with ParallelTestExecution {

  it should "retrieve the correct number of columns in a simple table" in {
    BasicTable.columns.length shouldEqual 4
  }

  it should "retrieve the correct number of columns in a big table" in {
    ComplexCompoundKeyTable.columns.length shouldEqual 10
  }

  it should "retrieve the correct number of primary keys for a table" in {
    SimpleCompoundKeyTable.primaryKeys.length shouldEqual 2
    SimpleCompoundKeyTable.partitionKeys.length shouldEqual 1
  }
}
