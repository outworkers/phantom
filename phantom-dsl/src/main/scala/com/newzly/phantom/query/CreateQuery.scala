package com.newzly.phantom.query

import scala.concurrent.{ Future => ScalaFuture }
import com.datastax.driver.core.{ ResultSet, Session }
import com.newzly.phantom.{ CassandraResultSetOperations, CassandraTable }
import com.twitter.util.Future

class CreateQuery[T <: CassandraTable[T, R], R](table: T, query: String) extends CassandraResultSetOperations {

  def execute()(implicit session: Session): Future[ResultSet] =  {
    queryStringExecuteToFuture(table.schema)
  }

  def future()(implicit session: Session): ScalaFuture[ResultSet] = {
    scalaQueryStringExecuteToFuture(table.schema)
  }
}