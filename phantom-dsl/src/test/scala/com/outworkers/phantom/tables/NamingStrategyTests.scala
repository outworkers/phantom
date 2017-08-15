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
package com.outworkers.phantom.tables

import com.outworkers.phantom.NamingStrategy
import org.scalatest.{FlatSpec, Matchers}

class NamingStrategyTests extends FlatSpec with Matchers {

  it should "not alter the name of a table if there is no macro in scope at definition site of the table" in {
    TestDatabase.recipes.tableName shouldEqual "recipes"
  }

  it should "convert a name to snake_case from camelCase" in {
    NamingStrategy.SnakeCase.caseInsensitive.inferName("tableName") shouldEqual "table_name"
  }

  it should "convert escape a name and convert to snake_case from camelCase" in {
    NamingStrategy.SnakeCase.caseSensitive.inferName("tableName") shouldEqual "'table_name'"
  }

  it should "convert escape a name and convert to camelCase from snake_case" in {
    NamingStrategy.CamelCase.caseSensitive.inferName("camel_case") shouldEqual "'camelCase'"
  }

  it should "preserve a table name using an identity strategy" in {
    NamingStrategy.Default.caseInsensitive.inferName("snake_case") shouldEqual "snake_case"
  }

  it should "escape and preserve a table name using an identity strategy" in {
    NamingStrategy.Default.caseSensitive.inferName("snake_case") shouldEqual "'snake_case'"
  }

  it should "convert a name and convert to camelCase from snake_case" in {
    NamingStrategy.CamelCase.caseInsensitive.inferName("snake_case") shouldEqual "snakeCase"
  }


  it should "convert and escape a name to camelCase from snake_case" in {
    NamingStrategy.CamelCase.caseSensitive.inferName("snake_case") shouldEqual "'snakeCase'"
  }

  it should "alter the name of a table if a NamingStrategy exists in scope" in {
    NamingStrategyDatabase.articlesByAuthor.tableName shouldEqual "'named_articles_by_author'"
  }
}
