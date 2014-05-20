package com.newzly.phantom.dsl.query

import org.scalatest.{ FlatSpec, Matchers, ParallelTestExecution }
import com.newzly.phantom.Implicits._
import com.newzly.phantom.tables.Primitives
import com.newzly.util.testing.Sampler

class IndexOperatorsRestrictionTests extends FlatSpec with Matchers {
  
    it should "allow using the eqs operator on index columns" in {
      "Primitives.select.where(_.pkey eqs Sampler.getARandomString)" should compile
    }

    it should "not allow using the eqs operator on non index columns" in {
      "Primitives.select.where(_.long eqs 5L)" shouldNot compile
    }

    it should "allow using the lt operator on index columns" in {
      "Primitives.select.where(_.pkey lt Sampler.getARandomString)" should compile
    }

    it should "not allow using the lt operator on non index columns" in {
      "Primitives.select.where(_.long lt 5L)" shouldNot compile
    }

    it should "allow using the lte operator on index columns" in {
      "Primitives.select.where(_.pkey lte Sampler.getARandomString)" should compile
    }

    it should "not allow using the lte operator on non index columns" in {
      "Primitives.select.where(_.long lte 5L)" shouldNot compile
    }

    it should "allow using the gt operator on index columns" in {
      "Primitives.select.where(_.pkey gt Sampler.getARandomString)" should compile
    }

    it should "not allow using the gt operator on non index columns" in {
      "Primitives.select.where(_.long gt 5L)" shouldNot compile
    }

    it should "allow using the gte operator on index columns" in {
      "Primitives.select.where(_.pkey gte Sampler.getARandomString)" should compile
    }

    it should "not allow using the gte operator on non index columns" in {
      "Primitives.select.where(_.long gte 5L)" shouldNot compile
    }

    it should "allow using the in operator on index columns" in {
      "Primitives.select.where(_.pkey in List(Sampler.getARandomString, Sampler.getARandomString))" should compile
    }

    it should "not allow using the in operator on non index columns" in {
      "Primitives.select.where(_.long in List(5L, 6L))" shouldNot compile
    }
}
