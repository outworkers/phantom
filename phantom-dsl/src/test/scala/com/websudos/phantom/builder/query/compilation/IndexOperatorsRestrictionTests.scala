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
package com.websudos.phantom.builder.query.compilation

import com.websudos.phantom.builder.query.KeySpaceSuite
import com.websudos.phantom.dsl._
import com.websudos.phantom.tables.TestDatabase
import com.outworkers.util.testing._
import org.scalatest.{FlatSpec, Matchers}

class IndexOperatorsRestrictionTests extends FlatSpec with Matchers with KeySpaceSuite {

  val s = gen[String]
  val Primitives = TestDatabase.primitives
  val b = Batch.logged

  it should "allow using the eqs operator on index columns" in {
    "Primitives.select.where(_.pkey eqs gen[String])" should compile
  }

  it should "not allow using the eqs operator on non index columns" in {
    "Primitives.select.where(_.long eqs 5L)" shouldNot compile
  }

  it should "allow using the lt operator on index columns" in {
    "Primitives.select.where(_.pkey lt gen[String])" should compile
  }

  it should "not allow using the lt operator on non index columns" in {
    "Primitives.select.where(_.long lt 5L)" shouldNot compile
  }

  it should "allow using the lte operator on index columns" in {
    "Primitives.select.where(_.pkey lte gen[String])" should compile
  }

  it should "not allow using the lte operator on non index columns" in {
    "Primitives.select.where(_.long lte 5L)" shouldNot compile
  }

  it should "allow using the gt operator on index columns" in {
    "Primitives.select.where(_.pkey gt gen[String])" should compile
  }

  it should "not allow using the gt operator on non index columns" in {
    "Primitives.select.where(_.long gt 5L)" shouldNot compile
  }

  it should "allow using the gte operator on index columns" in {
    "Primitives.select.where(_.pkey gte gen[String])" should compile
  }

  it should "not allow using the gte operator on non index columns" in {
    "Primitives.select.where(_.long gte 5L)" shouldNot compile
  }

  it should "allow using the in operator on index columns" in {
    "Primitives.select.where(_.pkey in List(gen[String], gen[String]))" should compile
  }

  it should "not allow using the in operator on non index columns" in {
    "Primitives.select.where(_.long in List(5L, 6L))" shouldNot compile
  }
}
