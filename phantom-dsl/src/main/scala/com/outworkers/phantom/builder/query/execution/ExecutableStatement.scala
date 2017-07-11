package com.outworkers.phantom.builder.query.execution

import com.datastax.driver.core.{Session, Statement}
import com.outworkers.phantom.ResultSet
import com.outworkers.phantom.builder.query.CassandraOperations

import scala.concurrent.{ExecutionContextExecutor, Future}

trait ExecutableStatement extends QueryInterface[Future] with CassandraOperations {

  override protected[this] def fromGuava(st: Statement)(
    implicit session: Session,
    executor: ExecutionContextExecutor
  ): Future[ResultSet] = statementToFuture(st)
}
