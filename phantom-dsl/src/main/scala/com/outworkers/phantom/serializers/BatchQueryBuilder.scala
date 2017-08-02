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

private[phantom] class BatchQueryBuilder {

  def batch(batchType: String): CQLQuery = {
    CQLQuery(CQLSyntax.Batch.begin).pad.append(batchType).pad.append(CQLSyntax.Batch.batch)
  }

  def applyBatch(qb: CQLQuery): CQLQuery = {
    qb.forcePad.append(CQLSyntax.Batch.apply)
      .forcePad.append(CQLSyntax.Batch.batch)
      .append(CQLSyntax.Symbols.semicolon)
  }
}