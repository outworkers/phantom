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

import com.outworkers.phantom.builder.query.engine.CQLQuery
import com.outworkers.phantom.builder.syntax.CQLSyntax

class SASIQueryBuilder extends BaseModifiers {

  protected[this] def percentEscape(value: String): CQLQuery = {
    CQLQuery(CQLSyntax.Symbols.percent)
      .append(value)
      .append(CQLSyntax.Symbols.percent)
  }

  def likeAny(column: String, value: String): CQLQuery = {
    CQLQuery(column).forcePad
      .append(CQLSyntax.Operators.like)
      .forcePad.append(value)
  }

  def containsValue(value: String): CQLQuery = {
    CQLQuery.empty.appendSingleQuote(percentEscape(value))
  }

  def prefixValue(value: String): CQLQuery = {
    CQLQuery.empty.appendSingleQuote(value + CQLSyntax.Symbols.percent)
  }

  def suffixValue(value: String): CQLQuery = {
    CQLQuery.empty.appendSingleQuote(CQLSyntax.Symbols.percent + value)
  }
}
