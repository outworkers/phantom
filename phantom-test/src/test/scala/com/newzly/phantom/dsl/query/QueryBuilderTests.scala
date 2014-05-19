package com.newzly.phantom.dsl.query

import org.scalatest.{ FeatureSpec, Matchers, ParallelTestExecution }
import com.newzly.phantom.tables.Primitives
import com.newzly.util.testing.Sampler

class QueryBuilderTests extends FeatureSpec with Matchers with ParallelTestExecution {
  feature("Select Query clauses") {
    scenario("Should not allow chaining two where clauses") {
      "Primitives.select.where(_.pkey eqs Sampler.getARandomString).where(_.pkey eqs Sampler.getARandomString)" shouldNot compile
    }
  }
}
