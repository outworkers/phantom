package net.liftweb.cassandra.blackpepper.query

import com.datastax.driver.core.querybuilder.{ Clause, Select }
import com.datastax.driver.core.Row

import net.liftweb.cassandra.blackpepper.{ CassandraTable }

class SelectQuery[T <: CassandraTable[T, _], R](val table: T, val qb: Select, rowFunc: Row => R) extends ExecutableQuery[T, R] {

  override def fromRow(r: Row) = rowFunc(r)

  def where(c: T => Clause) = {
    new SelectWhere[T, R](table, qb.where(c(table)), fromRow)
  }

  def limit(l: Int) = {
    new SelectQuery(table, qb.limit(l), fromRow)
  }
}

class SelectWhere[T <: CassandraTable[T, _], R](val table: T, val qb: Select.Where, rowFunc: Row => R) extends ExecutableQuery[T, R] {

  override def fromRow(r: Row) = rowFunc(r)

  def where(c: T => Clause) = {
    new SelectWhere[T, R](table, qb.and(c(table)), fromRow)
  }

  def limit(l: Int) = {
    new SelectQuery(table, qb.limit(l), fromRow)
  }

  def and = where _

}