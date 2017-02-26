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

private[builder] trait BaseModifiers {

  protected[this] def modifier(column: String, op: String, value: String): CQLQuery = {
    CQLQuery(column).forcePad.append(op).forcePad.append(value)
  }

  protected[this] def modifier(column: String, op: String, value: CQLQuery): CQLQuery = {
    modifier(column, op, value.queryString)
  }

  protected[this] def collectionModifier(left: String, op: String, right: CQLQuery): CQLQuery = {
    CQLQuery(left).forcePad.append(op).forcePad.append(right)
  }

  protected[this] def collectionModifier(left: String, op: String, right: String): CQLQuery = {
    CQLQuery(left).forcePad.append(op).forcePad.append(right)
  }
}
