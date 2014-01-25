package com.newzly.phantom.query

import com.datastax.driver.core.{ ResultSet, Session }
import com.newzly.phantom.{ AbstractColumn, CassandraResultSetOperations, CassandraTable }
import com.twitter.util.Future

class CreateQuery[T <: CassandraTable[T, R], R](table: T, query: String) extends CassandraResultSetOperations {
  def apply(columns: (T => AbstractColumn[_])*): CreateQuery[T, R] = {

    val queryInit = s"CREATE TABLE ${table.tableName} ("
    val queryColumns = columns.foldLeft("")((qb, c) => {
      val col = c(table)
      s"$qb, ${col.name} ${col.cassandraType}"
    })

    val pk = table._key.name
    //TODO support multiple keys

    val queryPrimaryKey  = s", PRIMARY KEY ($pk)"
    new CreateQuery(table, queryInit + queryColumns.drop(1) + queryPrimaryKey+ ");")
  }

  val queryString = query

  def execute()(implicit session: Session): Future[ResultSet] =  {
    queryStringExecuteToFuture(query)
  }

}
