package com.websudos.phantom.builder.query

import com.websudos.phantom.builder.QueryBuilder
import org.scalatest.{Matchers, FreeSpec}

class CollectionModifiersSerialisationTest extends FreeSpec with Matchers {

  "The collection modifier query builder" - {

    "should append a single element to a list<type> collection" in {
      QueryBuilder.append("test", "test1", "test2", "test3").queryString shouldEqual "test = test + [test1]"
    }

    "should append elements to a list<type> collection" in {
      QueryBuilder.append("test", "test1", "test2", "test3").queryString shouldEqual "test = test + [test1, test2, test3]"
    }


    "should prepend a single element to a list<type> collection" in {
      QueryBuilder.prepend("test", "test1") shouldEqual "test = [test1] + test"
    }

    "should prepend multiple elements to a list<type> collection" in {
      QueryBuilder.prepend("test", "test1", "test2", "test3") shouldEqual "test = [test1, test2, test3] + test"
    }
  }
}
