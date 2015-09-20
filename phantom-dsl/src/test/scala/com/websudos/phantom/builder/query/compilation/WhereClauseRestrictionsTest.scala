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
 * - Explicit consent must be obtained from the copyright owner, Websudos Limited before any redistribution is made.
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
import com.websudos.util.testing._
import org.scalatest.{FlatSpec, Matchers}

class WhereClauseRestrictionsTest extends FlatSpec with Matchers with KeySpaceSuite {


  val s = gen[String]
  val Primitives = TestDatabase.primitives
  val c = context

  it should "allow using a Select.Where clause" in {
    "Primitives.select.where(_.pkey eqs gen[String])" should compile
  }

  it should "allow using a Select.Where clause with AND chain" in {
    "Primitives.select.where(_.pkey eqs gen[String]).and(_.pkey eqs gen[String])" should compile
  }

  it should "not allow chaining two  Select.Where clauses" in {
    "Primitives.select.where(_.pkey eqs gen[String]).where(_.pkey eqs gen[String])" shouldNot compile
  }

  it should "not allow re-using a Where clause after an WHERE/AND chain" in {
    "Primitives.select.where(_.pkey eqs gen[String]).and(_.pkey eqs gen[String]).where(_.pkey eqs gen[String])" shouldNot compile
  }

  it should "Should not allow chaining two Update.Where clauses" in {
    "Primitives.update.where(_.pkey eqs gen[String]).where(_.pkey eqs gen[String])" shouldNot compile
  }

  it should "not allow chaining two Delete.Where clauses" in {
    "Primitives.update.where(_.pkey eqs gen[String]).where(_.pkey eqs gen[String])" shouldNot compile
  }
}
