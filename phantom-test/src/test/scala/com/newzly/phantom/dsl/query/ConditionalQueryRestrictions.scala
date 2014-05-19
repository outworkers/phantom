package com.newzly.phantom.dsl.query

import org.scalatest.{ FlatSpec, Matchers, ParallelTestExecution }
import com.datastax.driver.core.utils.UUIDs
import com.newzly.phantom.Implicits._
import com.newzly.phantom.tables.{ Primitives, TimeSeriesTable }
import com.newzly.util.testing.Sampler

class ConditionalQueryRestrictions extends FlatSpec with Matchers with ParallelTestExecution {

  it should "allow using a non-index column in a conditional update clause" in {
    "Primitives.update.where(_.pkey eqs Sampler.getARandomString).onlyIf(_.long eqs 5L)" should compile
  }

  it should " not allow using a PartitionKey in a conditional clause" in {
    "Primitives.update.where(_.pkey eqs Sampler.getARandomString).onlyIf(_.pkey eqs Sampler.getARandomString)" shouldNot compile
  }

  it should " not allow using a PrimaryKey in a conditional clause " in {
    "TwoKeys.update.where(_.pkey eqs Sampler.getARandomString).onlyIf(_.intColumn1 eqs 5)" shouldNot compile
  }

  it should " not allow using an Index in a conditional clause " in {
    "SecondaryIndexTable.update.where(_.id eqs UUIDs.timeBased()).onlyIf(_.secondary eqs UUIDs.timeBased())" shouldNot compile
  }

  it should " allow using a non Clustering column from a TimeSeries table in a conditional clause" in {
    "TimeSeriesTable.update.where(_.id eqs UUIDs.timeBased()).onlyIf(_.name eqs Sampler.getARandomString)" should compile
  }

  it should " not allow using a ClusteringColumn in a conditional clause" in {
    "TimeSeriesTable.update.where(_.id eqs UUIDs.timeBased()).onlyIf(_.timestamp eqs new DateTime)" shouldNot compile
  }
}
