package com.newzly.phantom.query

import com.datastax.driver.core.{ ResultSet, Session }
import com.twitter.util.Future
import com.newzly.phantom.{ CassandraResultSetOperations, CassandraTable }
import com.newzly.phantom.column.{ AbstractColumn, Column }
import java.util.Date
import org.joda.time.DateTime

class CreateQuery[T <: CassandraTable[T, R], R](table: T, query: String) extends CassandraResultSetOperations {
  def apply(columns: (T => AbstractColumn[_])*): CreateQuery[T, R] = {

    val queryInit = s"CREATE TABLE ${table.tableName} ("
    val queryColumns = columns.foldLeft("")((qb, c) => {
      val col = c(table)
      s"$qb, ${col.name} ${col.cassandraType}"
    })

    val pkes = table.primaryKeys.map(_.name).mkString(",")
    val queryPrimaryKey  = s", PRIMARY KEY ($pkes)"
    new CreateQuery(table, queryInit + queryColumns.drop(1) + queryPrimaryKey + ");")
  }

  val queryString = query

  def withClusteringOrder(column: Column[T, R, DateTime]): OrderedQuery[T, R] = {
    table.addKey(column)
    new OrderedQuery[T, R](table, query + "")
  }

  def execute()(implicit session: Session): Future[ResultSet] =  {
    queryStringExecuteToFuture(query)
  }
}

class OrderedQuery[T <: CassandraTable[T, R], R](table: T, query: String) extends CreateQuery[T, R](table, query) {
  def descending: OrderedQuery[T, R] = {
    new OrderedQuery[T, R](table, query + " DESC)")
  }

  def ascending: OrderedQuery[T, R] = {
    new OrderedQuery[T, R](table, query + " ASCENDING)")
  }
}