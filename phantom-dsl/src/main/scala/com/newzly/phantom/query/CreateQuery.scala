package com.newzly.phantom.query

import scala.concurrent.{ ExecutionContext, Future => ScalaFuture }
import com.datastax.driver.core.{ ResultSet, Session }
import com.newzly.phantom.{ CassandraResultSetOperations, CassandraTable }

class CreateQuery[T <: CassandraTable[T, R], R](val table: T, query: String) extends CassandraResultSetOperations {

  def future()(implicit session: Session, context: ExecutionContext): ScalaFuture[ResultSet] = {
    if (table.createIndexes().isEmpty)
      scalaQueryStringExecuteToFuture(table.schema())
    else {
      scalaQueryStringExecuteToFuture(table.schema())  flatMap {
      _=> {
        ScalaFuture.sequence(table.createIndexes() map scalaQueryStringExecuteToFuture) map (_.head)
      }
      }
    }
  }
}