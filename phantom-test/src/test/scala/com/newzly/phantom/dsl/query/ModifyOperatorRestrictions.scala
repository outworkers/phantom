package com.newzly.phantom.dsl.query

import org.scalatest.{ FlatSpec, Matchers, ParallelTestExecution }
import com.newzly.phantom.tables.{ CounterTableTest, TimeSeriesTable, TwoKeys }

class ModifyOperatorRestrictions extends FlatSpec with Matchers with ParallelTestExecution {
  
  it should "not allow using the setTo operator on a Counter column" in {
    "CounterTableTest.update.where(_.id eqs UUIDs.timeBased()).modify(_.count_entries setTo 5L)" shouldNot compile
  }

  it should "not allow using the setTo operator on a PartitionKey" in {
    "CounterTableTest.update.where(_.id eqs UUIDs.timeBased()).modify(_.id setTo UUIDs.timeBased())" shouldNot compile
  }

  it should "not allow using the setTo operator on a PrimaryKey" in {
    "TwoKeys.update.where(_.pkey eqs UUIDs.timeBased().toString).modify(_.pkey setTo UUIDs.timeBased().toString)" shouldNot compile
  }

  it should "allow using setTo operators for non index columns" in {
    """TimeSeriesTable.update.where(_.id eqs UUIDs.timeBased()).modify(_.name setTo "test")""" shouldNot compile
  }

  it should "not allow using the setTo operator on a Clustering column" in {
    "TimeSeriesTable.update.where(_.id eqs UUIDs.timeBased()).modify(_.timestamp setTo new DateTime)" shouldNot compile
  }
}
