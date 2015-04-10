package com.websudos.phantom.builder.serializers

import com.websudos.phantom.builder.query.CQLQuery

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
