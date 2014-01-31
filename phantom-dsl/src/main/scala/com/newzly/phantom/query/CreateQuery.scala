package com.newzly.phantom.query

import scala.concurrent.{ Future => ScalaFuture }
import com.datastax.driver.core.{ ResultSet, Session }
import com.newzly.phantom.{ CassandraResultSetOperations, CassandraTable }
import com.twitter.util.Future
import scala.concurrent.ExecutionContext.Implicits.global

class CreateQuery[T <: CassandraTable[T, R], R](table: T, query: String) extends CassandraResultSetOperations {

  def execute()(implicit session: Session): Future[ResultSet] =  {
    if (table.createIndexes().isEmpty)
      queryStringExecuteToFuture(table.schema)
    else
      queryStringExecuteToFuture(table.schema)  flatMap {
        _=> {
         val seqF = table.createIndexes() map (q => queryStringExecuteToFuture(q))
         val f = seqF.reduce[Future[ResultSet]]((f1,f2) => f1 flatMap { _ => f2 })
         f
        }
      }
  }

  def future()(implicit session: Session): ScalaFuture[ResultSet] = {
    if (table.createIndexes().isEmpty)
      scalaQueryStringExecuteToFuture(table.schema)
    else
      scalaQueryStringExecuteToFuture(table.schema)  flatMap {
      _=> {
        val seqF = table.createIndexes() map (q => scalaQueryStringExecuteToFuture(q))
        val f = seqF.reduce[ScalaFuture[ResultSet]]((f1,f2) => f1 flatMap { _ => f2 })
        f
      }
    }
  }
}