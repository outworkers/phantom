package com.newzly.phantom.batch

import com.newzly.phantom.query.ExecutableStatement
import com.datastax.driver.core.{BatchStatement => DatastaxBatchStatement, ResultSet, Session}
import com.newzly.phantom.CassandraResultSetOperations
import scala.collection.mutable.ListBuffer
import scala.concurrent.Future

sealed trait BatchQueryListTrait extends CassandraResultSetOperations {
  protected[this] lazy val statements: Iterator[ExecutableStatement] =  Iterator.empty
  def add(statement: => ExecutableStatement): BatchStatement
  def future()(implicit session: Session): Future[ResultSet]
  def qbList: Iterator[ExecutableStatement]
}

/**
 * !!! Attention !!!
 * This class is not meant to be used for concurrent operations on the same row inside one batch.
 * In order to have concurrent operation on the same row in the same batch, custom timesatmps needs to be inserted
 * on each statement. This is not in the scope of this class.(for now)
 */
class BatchStatement(val qbList: Iterator[ExecutableStatement] = Iterator.empty) extends BatchQueryListTrait {

  def apply(list: Iterator[ExecutableStatement] = Iterator.empty) = {
    new BatchStatement(list)
  }

  def add(statement: => ExecutableStatement): BatchStatement = {
     apply(qbList ++ Iterator(statement))
  }

  def future()(implicit session: Session): Future[ResultSet] = {
    val batch = new DatastaxBatchStatement()
    for (s <- qbList) {
      batch.add(s.qb)
    }
    scalaStatementToFuture(batch)
  }
}