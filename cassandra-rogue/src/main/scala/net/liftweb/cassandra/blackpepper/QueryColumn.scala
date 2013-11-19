package net.liftweb.cassandra.blackpepper

import com.datastax.driver.core.querybuilder.{ Assignment, QueryBuilder, Clause }
import scala.collection.JavaConverters._
import com.datastax.driver.core.Row

abstract class AbstractQueryColumn[RR: CSPrimitive](col: Column[RR]) {

  def eqs(value: RR): Clause = QueryBuilder.eq(col.name, CSPrimitive[RR].toCType(value))

  def in[L <% Traversable[RR]](vs: L) = QueryBuilder.in(col.name, vs.map(CSPrimitive[RR].toCType).toSeq: _*)

  def gt(value: RR): Clause = QueryBuilder.gt(col.name, CSPrimitive[RR].toCType(value))
  def gte(value: RR): Clause = QueryBuilder.gte(col.name, CSPrimitive[RR].toCType(value))
  def lt(value: RR): Clause = QueryBuilder.lt(col.name, CSPrimitive[RR].toCType(value))
  def lte(value: RR): Clause = QueryBuilder.lte(col.name, CSPrimitive[RR].toCType(value))
}

class QueryColumn[RR: CSPrimitive](col: Column[RR]) extends AbstractQueryColumn[RR](col)

abstract class AbstractModifyColumn[RR](name: String) {

  def toCType(v: RR): AnyRef

  def setTo(value: RR): Assignment = QueryBuilder.set(name, toCType(value))
}

class ModifyColumn[RR](col: AbstractColumn[RR]) extends AbstractModifyColumn[RR](col.name) {

  def toCType(v: RR): AnyRef = col.toCType(v)
}

class ModifyColumnOptional[RR](col: OptionalColumn[RR]) extends AbstractModifyColumn[Option[RR]](col.name) {

  def toCType(v: Option[RR]): AnyRef = v.map(col.toCType).orNull
}

abstract class SelectColumn[T](val col: AbstractColumn[_]) {

  def apply(r: Row): T
}

class SelectColumnRequired[T](override val col: Column[T]) extends SelectColumn[T](col) {

  def apply(r: Row): T = col.apply(r)
}

class SelectColumnOptional[T](override val col: OptionalColumn[T]) extends SelectColumn[Option[T]](col) {

  def apply(r: Row): Option[T] = col.apply(r)

}

//class CSPrimitiveModifyColumn[RR: CSPrimitive](name: String) extends AbstractModifyColumn[RR](name) {
//
//  def toCType(v: RR): AnyRef = implicitly[CSPrimitive[RR]].toCType(v)
//}
//
//class JsonTypeModifyColumn[RR: Format](name: String) extends AbstractModifyColumn[RR](name) {
//
//  def toCType(v: RR): AnyRef = Json.stringify(Json.toJson(v))
//}
//
//abstract class AbstractSeqModifyColumn[RR](name: String) {
//
//  def toCType(v: RR): AnyRef
//
//  def setTo(values: Seq[RR]): Assignment = QueryBuilder.set(name, values.map(toCType).asJava)
//}
//
//class CSPrimitiveSeqModifyColumn[RR: CSPrimitive](name: String) extends AbstractSeqModifyColumn[RR](name) {
//
//  def toCType(v: RR): AnyRef = implicitly[CSPrimitive[RR]].toCType(v)
//}
//
//class JsonTypeSeqModifyColumn[RR: Format](name: String) extends AbstractSeqModifyColumn[RR](name) {
//
//  def toCType(v: RR): AnyRef = Json.stringify(Json.toJson(v))
//}