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
package com.outworkers.phantom.builder.query

import com.datastax.driver.core.ConsistencyLevel
import org.scalatest.{FlatSpec, Matchers, OptionValues}
import com.outworkers.util.samplers._

class QueryOptionsTest extends FlatSpec with OptionValues with Matchers {

  it should "allow setting a consistency level" in {
    val level = ConsistencyLevel.ALL
    val options = QueryOptions.empty.consistencyLevel_=(level)
    options.consistencyLevel shouldBe defined
    options.consistencyLevel.value shouldEqual level
  }

  it should "allow setting a serial consistency level" in {
    val level = ConsistencyLevel.LOCAL_QUORUM
    val options = QueryOptions.empty.serialConsistencyLevel_=(level)
    options.serialConsistencyLevel shouldBe defined
    options.serialConsistencyLevel.value shouldEqual level
  }

  it should "allow setting the enable tracing property" in {
    val bool = gen[Boolean]
    val options = QueryOptions.empty.enableTracing_=(bool)
    options.enableTracing shouldBe defined
    options.enableTracing.value shouldEqual bool
  }

  it should "allow setting a fetch size" in {
    val size = gen[Int]
    val options = QueryOptions.empty.fetchSize_=(size)
    options.fetchSize shouldBe defined
    options.fetchSize.value shouldEqual size
  }

  it should "allow setting an entire sequence of options" in {
    val size = gen[Int]
    val bool = gen[Boolean]

    val options = QueryOptions.empty
      .enableTracing_=(bool)
      .fetchSize_=(size)
      .serialConsistencyLevel_=(ConsistencyLevel.ALL)
      .consistencyLevel_=(ConsistencyLevel.QUORUM)

    options.fetchSize shouldBe defined
    options.fetchSize.value shouldEqual size

    options.enableTracing shouldBe defined
    options.enableTracing.value shouldEqual bool

    options.consistencyLevel shouldBe defined
    options.consistencyLevel.value shouldEqual ConsistencyLevel.QUORUM

    options.serialConsistencyLevel shouldBe defined
    options.serialConsistencyLevel.value shouldEqual ConsistencyLevel.ALL
  }
}
