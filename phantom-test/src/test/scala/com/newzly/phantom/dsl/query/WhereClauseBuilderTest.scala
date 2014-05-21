package com.newzly.phantom.dsl.query

import org.scalatest.{ FlatSpec, Matchers, ParallelTestExecution }
import com.newzly.phantom.Implicits._
import com.newzly.phantom.tables.Primitives
import com.newzly.util.testing.Sampler

class WhereClauseBuilderTest extends FlatSpec with Matchers {


  it should "allow using a Select.Where clause" in {
    "Primitives.select.where(_.pkey eqs Sampler.getARandomString)" should compile
  }

  it should "allow using a Select.Where clause with AND chain" in {
    "Primitives.select.where(_.pkey eqs Sampler.getARandomString).and(_.pkey eqs Sampler.getARandomString)" should compile
  }

  it should "not allow chaining two  Select.Where clauses" in {
    "Primitives.select.where(_.pkey eqs Sampler.getARandomString).where(_.pkey eqs Sampler.getARandomString)" shouldNot compile
  }

  it should "not allow re-using a Where clause after an WHERE/AND chain" in {
    "Primitives.select.where(_.pkey eqs Sampler.getARandomString).and(_.pkey eqs Sampler.getARandomString).where(_.pkey eqs Sampler.getARandomString)" shouldNot compile
  }

  it should "Should not allow chaining two Update.Where clauses" in {
    "Primitives.update.where(_.pkey eqs Sampler.getARandomString).where(_.pkey eqs Sampler.getARandomString)" shouldNot compile
  }

  it should "not allow chaining two Delete.Where clauses" in {
    "Primitives.update.where(_.pkey eqs Sampler.getARandomString).where(_.pkey eqs Sampler.getARandomString)" shouldNot compile
  }
}
