package com.websudos.phantom.builder.serializers

import com.websudos.phantom.builder.query.CQLQuery
import com.websudos.phantom.builder.syntax.CQLSyntax

private[phantom] class BatchQueryBuilder {
  def batch(batchType: String): CQLQuery = {
    CQLQuery(CQLSyntax.Batch.begin).forcePad.append(batchType).forcePad.append(CQLSyntax.Batch.batch)
  }

  def applyBatch(qb: CQLQuery): CQLQuery = {
    qb.forcePad.append(CQLSyntax.Batch.apply).forcePad.append(CQLSyntax.Batch.batch).append(CQLSyntax.Symbols.`;`)
  }
}