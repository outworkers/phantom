package com.newzly.phantom.batch

import com.newzly.phantom.query.{ ExecutableStatement }
import com.datastax.driver.core.{BatchStatement => DatastaxBatchStatement, ResultSet, Session}
import com.newzly.phantom.CassandraResultSetOperations
import com.twitter.util.Future
import scala.collection.parallel.mutable.ParHashSet
import scala.collection.mutable.ListBuffer

sealed trait BatchQueryListTrait extends CassandraResultSetOperations{
  protected[this] lazy val statements: ListBuffer[ExecutableStatement] =  ListBuffer.empty[ExecutableStatement]
  def add(statement: ExecutableStatement)
  def execute()(implicit session: Session) :Future[ResultSet]
}

class BatchStatement extends BatchQueryListTrait {

  def add(statement: ExecutableStatement): Unit = {
    statements += statement
  }

  def execute()(implicit session: Session):Future[ResultSet] = {
    val batch = new DatastaxBatchStatement()
    for (s <- statements) {
      Console.println(s.qb)
      batch.add(s.qb)
    }
    statementExecuteToFuture(batch)
  }
}