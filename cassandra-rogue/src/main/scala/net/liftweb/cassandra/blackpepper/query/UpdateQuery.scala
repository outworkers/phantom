package net.liftweb.cassandra.blackpepper.query

import com.datastax.driver.core.querybuilder.{ Assignment, Clause, Update }
import net.liftweb.cassandra.blackpepper.{ CassandraTable => CTable }

class UpdateQuery[T <: CTable[T, R], R](table: T, val qb: Update) {

  def where(c: T => Clause): UpdateWhere[T, R] = {
    new UpdateWhere[T, R](table, qb.where(c(table)))
  }

}

class UpdateWhere[T <: CTable[T, R], R](table: T, val qb: Update.Where) {

  def where(c: T => Clause): UpdateWhere[T, R] = {
    new UpdateWhere[T, R](table, qb.and(c(table)))
  }

  def and = where _

  def modify(a: T => Assignment): AssignmentsQuery[T, R] = {
    new AssignmentsQuery[T, R](table, qb.`with`(a(table)))
  }

}

class AssignmentsQuery[T <: CTable[T, R], R](table: T, val qb: Update.Assignments) extends ExecutableStatement {

  def modify(a: T => Assignment): AssignmentsQuery[T, R] = {
    new AssignmentsQuery[T, R](table, qb.and(a(table)))
  }

  def and = modify _

}