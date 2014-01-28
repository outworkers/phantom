package com.newzly.phantom.query

import com.datastax.driver.core.{ ResultSet, Session }
import com.newzly.phantom.{ CassandraResultSetOperations, CassandraTable }
import com.newzly.phantom.Implicits.DateTimeColumn
import com.twitter.util.Future

class CreateQuery[T <: CassandraTable[T, R], R](table: T, query: String) extends CassandraResultSetOperations {
  def apply(): CreateQuery[T, R] = {
    val queryInit = s"CREATE TABLE ${table.tableName} ("
    val queryColumns = table.columns.foldLeft("")((qb, c) => {
      s"$qb, ${c.name} ${c.cassandraType}"
    })

    val pkes = table.primaryKeys.map(_.name).mkString(",")
    table.logger.info(s"Adding Primary keys indexes: $pkes")
    val queryPrimaryKey  = if (pkes.length > 0) s", PRIMARY KEY ($pkes)" else ""
    new CreateQuery(table, queryInit + queryColumns.drop(1) + queryPrimaryKey + ")")
  }

  def queryString: String = {
    if (query.last != ';') query + ";" else query
  }

  def withClusteringOrder(columnRef: T => DateTimeColumn[T, R]): OrderedQuery[T, R] = {
    val column = columnRef(table)
    table.addKey(column)
    new OrderedQuery[T, R](table, query + s" WITH CLUSTERING ORDER BY (${column.name}")
  }

  def execute()(implicit session: Session): Future[ResultSet] =  {
    queryStringExecuteToFuture(queryString)
  }
}

class OrderedQuery[T <: CassandraTable[T, R], R](table: T, query: String) {
  def descending: CreateQuery[T, R] = {
    new CreateQuery[T, R](table, query + " DESC);")
  }

  def ascending: CreateQuery[T, R] = {
    new CreateQuery[T, R](table, query + " ASCENDING);")
  }
}