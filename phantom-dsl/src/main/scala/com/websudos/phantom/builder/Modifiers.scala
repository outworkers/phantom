package com.websudos.phantom.builder

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



private[builder] trait CollectionModifiers extends BaseModifiers {

  def prepend(column: String, values: String*): CQLQuery = {
    collectionModifier(Utils.collection(values).queryString, CQLSyntax.Symbols.+, column)
  }

  def append(column: String, values: String*): CQLQuery = {
    collectionModifier(Utils.collection(values).queryString, CQLSyntax.Symbols.+, column)
  }

  def discard(column: String, values: String*): CQLQuery = {
    collectionModifier(Utils.collection(values).queryString, CQLSyntax.Symbols.-, column)
  }

  def add(column: String, values: Set[String]): CQLQuery = {
    collectionModifier(Utils.set(values).queryString, CQLSyntax.Symbols.+, column)
  }

  def remove(column: String, values: Set[String]): CQLQuery = {
    collectionModifier(Utils.set(values).queryString, CQLSyntax.Symbols.-, column)
  }

  def mapSet(column: String, key: String, value: String): CQLQuery = {
    CQLQuery(column).append(CQLSyntax.Symbols.`[`)
      .append(key).append(CQLSyntax.Symbols.`]`)
      .forcePad.append(CQLSyntax.eqs)
      .forcePad.append(value)
  }

  def setIdX(column: String, index: String, value: String): CQLQuery = {
    CQLQuery(column).append(CQLSyntax.Symbols.`[`)
      .append(index).append(CQLSyntax.Symbols.`]`)
      .forcePad.append(CQLSyntax.eqs)
      .forcePad.append(value)
  }

  def put(column: String, pairs: (String, String)*): CQLQuery = {
    collectionModifier(column, CQLSyntax.Symbols.+, Utils.map(pairs))
  }

  def serialize(col: Map[String, String] ): CQLQuery = {
    CQLQuery(CQLSyntax.Symbols.`{`).forcePad
      .append(CQLQuery(col.map(item => s"${item._1} : ${item._2}")))
      .forcePad.append(CQLSyntax.Symbols.`}`)
  }

  def mapType(keyType: String, valueType: String): CQLQuery = {
    CQLQuery(CQLSyntax.Collections.map)
      .append(CQLSyntax.Symbols.`<`)
      .append(keyType).append(CQLSyntax.Symbols.`,`)
      .append(valueType).append(CQLSyntax.Symbols.`>`)
  }

}


private[builder] class IndexModifiers extends BaseModifiers {

  def eqs(column: String, value: String): CQLQuery = {
    modifier(column, CQLSyntax.Operators.eqs, value)
  }

  def ==(column: String, value: String): CQLQuery = {
    modifier(column, CQLSyntax.Operators.eqs, value)
  }

  def lt(column: String, value: String): CQLQuery = {
    modifier(column, CQLSyntax.Operators.lt, value)
  }

  def lte(column: String, value: String): CQLQuery = {
    modifier(column, CQLSyntax.Operators.lte, value)
  }

  def gt(column: String, value: String): CQLQuery = {
    modifier(column, CQLSyntax.Operators.gt, value)
  }

  def gte(column: String, value: String): CQLQuery = {
    modifier(column, CQLSyntax.Operators.gte, value)
  }

  def in(column: String, values: String*): CQLQuery = {
    modifier(column, CQLSyntax.Operators.in, Utils.join(values))
  }

  def in(column: String, values: List[String]): CQLQuery = {
    modifier(column, CQLSyntax.Operators.in, Utils.join(values))
  }

  def fcall(name: String, params: String*): CQLQuery = {
    CQLQuery(name).append(Utils.join(params))
  }

  def token(name: String): String = {
    CQLQuery(CQLSyntax.token).wrap(name).queryString
  }

  def where(qb: CQLQuery, condition: CQLQuery): CQLQuery = {
    Utils.concat(qb, CQLSyntax.where, condition)
  }


  def and(qb: CQLQuery, clause: CQLQuery): CQLQuery = {
    Utils.concat(qb, CQLSyntax.and, clause)
  }

}
