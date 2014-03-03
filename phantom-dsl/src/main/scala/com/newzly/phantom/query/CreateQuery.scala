package com.newzly.phantom.query

import scala.concurrent.{Future => ScalaFuture, ExecutionContext}
import com.datastax.driver.core.{ ResultSet, Session }
import com.newzly.phantom.{Manager, CassandraResultSetOperations, CassandraTable}

class CreateQuery[T <: CassandraTable[T, R], R](val table: T, query: String) extends CassandraResultSetOperations {

  def future()(implicit session: Session, context: ExecutionContext): ScalaFuture[ResultSet] = {
    if (table.createIndexes().isEmpty)
      scalaQueryStringExecuteToFuture(table.schema())
    else
      scalaQueryStringExecuteToFuture(table.schema())  flatMap {
      _=> {
        val seqF = table.createIndexes() map (q => scalaQueryStringExecuteToFuture(q))
        val f = seqF.reduce[ScalaFuture[ResultSet]]((f1,f2) => f1 flatMap { _ => f2 })
        f
      }
    }
  }
}