package com.newzly.phantom.query

import scala.concurrent.Future
import com.datastax.driver.core.{ResultSet, Session}
import com.newzly.phantom.{CassandraTable, CassandraResultSetOperations}
import com.newzly.phantom.column.AbstractColumn

class SecondaryIndexesQuery[T <: CassandraTable[T, R], R](val table: CassandraTable[T, R], st: List[AbstractColumn[_]]) extends CassandraResultSetOperations {

  def future()(implicit session: Session): Future[Seq[ResultSet]] = {
    Future.sequence(st.map {
      index =>scalaQueryStringExecuteToFuture(s"CREATE INDEX ON $table (${index.name});")
    })
  }
}
