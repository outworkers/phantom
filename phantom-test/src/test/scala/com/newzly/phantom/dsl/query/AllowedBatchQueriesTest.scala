package com.newzly.phantom.dsl.query

import org.scalatest.{ FlatSpec, Matchers, ParallelTestExecution }
import com.newzly.phantom.Implicits._
import com.newzly.phantom.tables.{ Primitives, Recipes }
import com.newzly.util.testing.Sampler

class AllowedBatchQueriesTest extends FlatSpec with Matchers {

  it should "allow using Insert queries in a Batch statement" in {
    "BatchStatement().add(Primitives.insert)" should compile
  }

  it should " allow using an Insert.Value statement in a BatchStatement" in {
    "BatchStatement().add(Primitives.insert.value(_.long, 4L))" should compile
  }

  it should "allow using an Update.Assignments statement in a BatchStatement" in {
    "BatchStatement().add(Primitives.update.modify(_.long setTo 5L))" should compile
  }

  it should "allow using Update.Where queries in a BatchStatement" in {
    "BatchStatement().add(Primitives.update.where(_.pkey eqs Sampler.getARandomString))" should compile
  }

  it should "allow using Conditional Update.Where queries in a BatchStatement" in {
    "BatchStatement().add(Primitives.update.where(_.pkey eqs Sampler.getARandomString).onlyIf(_.long eqs 5L))" should compile
  }

  it should " allow using Conditional Assignments queries in a BatchStatement" in {
    "BatchStatement().add(Primitives.update.where(_.pkey eqs Sampler.getARandomString).modify(_.long setTo 10L).onlyIf(_.long eqs 5L))" should compile
  }

  it should " allow using Delete queries in a BatchStatement" in {
    "BatchStatement().add(Primitives.delete)" should compile
  }

  it should "Delete.Where queries in a BatchStatement" in {
    "BatchStatement().add(Primitives.delete)" should compile
  }
}
