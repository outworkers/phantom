package com.websudos.phantom.builder.serializers

import com.websudos.phantom.builder.QueryBuilder
import org.scalatest.{FreeSpec, Matchers}

class CollectionModifiersSerialisationTest extends FreeSpec with Matchers {

  "The collection modifier query builder" - {

    "should append a pre-serialized list as a collection" in {
      QueryBuilder.Collections.append("test", QueryBuilder.Utils.collection(List("test1", "test2")).queryString)
        .queryString shouldEqual "test = test + [test1, test2]"
    }

    "should append a single element to a list<type> collection" in {
      QueryBuilder.Collections.append("test", "test1", "test2").queryString shouldEqual "test = test + [test1, test2]"
    }

    "should append elements to a list<type> collection" in {
      QueryBuilder.Collections.append("test", "test1", "test2", "test3").queryString shouldEqual "test = test + [test1, test2, test3]"
    }

    "should prepend a pre-serialized list as a collection" in {
      QueryBuilder.Collections.prepend("test", QueryBuilder.Utils.collection(List("test1", "test2")).queryString)
        .queryString shouldEqual "test = [test1, test2] + test"
    }

    "should prepend a single element to a list<type> collection" in {
      QueryBuilder.Collections.prepend("test", "test1", "test2").queryString shouldEqual "test = [test1, test2] + test"
    }

    "should prepend multiple elements to a list<type> collection" in {
      QueryBuilder.Collections.prepend("test", "test1", "test2", "test3").queryString shouldEqual "test = [test1, test2, test3] + test"
    }
  }
}
