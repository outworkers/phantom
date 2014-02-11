package com.newzly.phantom.query

import scala.concurrent.{ Future => ScalaFuture }
import scala.concurrent.ExecutionContext.Implicits.global
import com.datastax.driver.core.{ ResultSet, Session }
import com.newzly.phantom.{ CassandraResultSetOperations, CassandraTable }

class CreateQuery[T <: CassandraTable[T, R], R](val table: T, query: String) extends CassandraResultSetOperations {

  def future()(implicit session: Session): ScalaFuture[ResultSet] = {
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