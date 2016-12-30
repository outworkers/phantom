/*
 * Copyright 2013 - 2017 Outworkers Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.outworkers.phantom.builder.serializers

import com.outworkers.phantom.builder.QueryBuilder
import org.scalatest.{FreeSpec, Matchers}

class CollectionModifiersSerialisationTest extends FreeSpec with Matchers {

  "The collection modifier query builder" - {

    "should append a pre-serialized list as a collection" in {
      QueryBuilder.Collections.append("test", QueryBuilder.Collections.serialize(List("test1", "test2")).queryString)
        .queryString shouldEqual "test = test + [test1, test2]"
    }

    "should append a single element to a list<type> collection" in {
      QueryBuilder.Collections.append("test", "test1", "test2").queryString shouldEqual "test = test + [test1, test2]"
    }

    "should append elements to a list<type> collection" in {
      QueryBuilder.Collections.append("test", "test1", "test2", "test3").queryString shouldEqual "test = test + [test1, test2, test3]"
    }

    "should prepend a pre-serialized list as a collection" in {
      QueryBuilder.Collections.prepend("test", QueryBuilder.Collections.serialize(List("test1", "test2")).queryString)
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
