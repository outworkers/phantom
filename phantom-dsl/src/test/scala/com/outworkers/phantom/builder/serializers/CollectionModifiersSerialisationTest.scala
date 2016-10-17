/*
 * Copyright 2013-2015 Websudos, Limited.
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * - Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 *
 * - Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *
 * - Explicit consent must be obtained from the copyright owner, Outworkers Limited before any redistribution is made.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package com.outworkers.phantom.builder.serializers

import com.outworkers.phantom.builder.QueryBuilder
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
