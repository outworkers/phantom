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
package com.outworkers.phantom.macros

import com.outworkers.phantom.tables.TestDatabase
import org.scalatest.{FlatSpec, Matchers}

class NamingStrategyTests extends FlatSpec with Matchers {

  it should "not alter the name of a table if there is no macro in scope at definition site of the table" in {
    TestDatabase.recipes.tableName shouldEqual "recipes"
  }

  it should "alter the name of a table if a NamingStrategy exists in scope" in {
    NamingStrategyDatabase.articlesByAuthor.tableName shouldEqual "'named_articles_by_author'"
  }
}
