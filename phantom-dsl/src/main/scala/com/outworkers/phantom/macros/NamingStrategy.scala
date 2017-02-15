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

import com.google.common.base.CaseFormat
import com.outworkers.phantom.builder.query.CQLQuery

trait NamingStrategy {
  def strategy: String

  type NameType <: NamingStrategy

  def inferName(name: String): String

  def apply(name: String): String = inferName(name)

  protected[this] def isCaseSensitive: Boolean = false

  def caseSensitive: NameType
  def caseInsensitive: NameType
}

object NamingStrategy {

  class CamelCase extends NamingStrategy {

    override type NameType = CamelCase

    override def strategy: String = "camel_case"

    override def inferName(name: String): String = {
      val source = CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_CAMEL, name)

      if (isCaseSensitive) {
        CQLQuery.escape(source)
      } else {
        source
      }
    }

    override def caseSensitive: CamelCase = new CamelCase {
      override protected[this] def isCaseSensitive: Boolean = true
    }

    override def caseInsensitive: CamelCase = new CamelCase {
      override protected[this] def isCaseSensitive: Boolean = false
    }
  }

  object CamelCase extends CamelCase

  class SnakeCase extends NamingStrategy {
    override def strategy: String = "snake_case"

    override def inferName(name: String): String = {
      val source = CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, name)

      if (isCaseSensitive) {
        CQLQuery.escape(source)
      } else {
        source
      }
    }

    override type NameType = SnakeCase

    override def caseSensitive: SnakeCase = new SnakeCase {
      override protected[this] def isCaseSensitive: Boolean = true
    }

    override def caseInsensitive: SnakeCase = new SnakeCase {
      override protected[this] def isCaseSensitive: Boolean = false
    }
  }

  object SnakeCase extends SnakeCase
}
