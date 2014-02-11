package com.newzly.phantom.finagle

import scala.collection.JavaConverters._
import com.datastax.driver.core.{ BatchStatement => DatastaxBatchStatement, ResultSet, Session, Statement}
import com.google.common.util.concurrent.{Futures, FutureCallback}
import com.newzly.phantom.{CassandraTable, CassandraResultSetOperations, Manager}
import com.newzly.phantom.batch.BatchStatement
import com.newzly.phantom.query._
import com.twitter.util.{Duration, Future, Promise}

object Implicits {

  implicit class FinagleResultSetOperations[T <: CassandraResultSetOperations](val op: T) extends AnyVal {

    /**
     * Converts a statement to a result Future.
     * @param s The statement to execute.
     * @param session The Cassandra cluster connection session to use.
     * @return
     */
    def statementExecuteToFuture(s: Statement)(implicit session: Session): Future[ResultSet] = {
      val promise = Promise[ResultSet]()
      val future = session.executeAsync(s)

      val callback = new FutureCallback[ResultSet] {
        def onSuccess(result: ResultSet): Unit = {
          promise become Future.value(result)
        }

        def onFailure(err: Throwable): Unit = {
          promise raise err
        }
      }
      Futures.addCallback(future, callback, Manager.executor)

      promise
    }

    def queryStringExecuteToFuture(s: String)(implicit session: Session): Future[ResultSet] = {
      val promise = Promise[ResultSet]()

      val future = session.executeAsync(s)

      val callback = new FutureCallback[ResultSet] {
        def onSuccess(result: ResultSet): Unit = {
          promise become Future.value(result)
        }

        def onFailure(err: Throwable): Unit = {
          promise raise err
        }
      }
      Futures.addCallback(future, callback, Manager.executor)
      promise
    }
  }

  implicit class FinagleBatchStatements[T <: BatchStatement](val st: T) extends AnyVal {

    def execute()(implicit session: Session):Future[ResultSet] = {
      val batch = new DatastaxBatchStatement()
      for (s <- st.qbList) {
        batch.add(s.qb)
      }
      st.statementExecuteToFuture(batch)
    }
  }

  implicit class FinagleCreateQuery[T <: CassandraTable[T, R] ,R](val query: CreateQuery[T, R]) extends AnyVal {

    def execute()(implicit session: Session): Future[ResultSet] =  {
      if (query.table.createIndexes().isEmpty)
        query.queryStringExecuteToFuture(query.table.schema())
      else
        query.queryStringExecuteToFuture(query.table.schema())  flatMap {
          _=> {
            val seqF = query.table.createIndexes() map (q => query.queryStringExecuteToFuture(q))
            val f = seqF.reduce[Future[ResultSet]]((f1,f2) => f1 flatMap { _ => f2 })
            f
          }
        }
    }
  }


  implicit class FinagleExecutableQuery[T <: CassandraTable[T, R], R](val query: ExecutableQuery[T, R]) extends AnyVal {

    def fetch(implicit session: Session): Future[Seq[R]] = {
      query.statementExecuteToFuture(query.qb).map(_.all().asScala.toSeq.map(query.fromRow))
    }

    def one(implicit session: Session): Future[Option[R]] = {
      query.statementExecuteToFuture(query.qb).map(r => Option(r.one()).map(query.fromRow))
    }
  }

  implicit class FinagleInsertQuery[T <: CassandraTable[T, R], R](val query: InsertQuery[T, R]) extends AnyVal {

    def execute()(implicit session: Session): Future[ResultSet] =
      query.statementExecuteToFuture(query.qb)

    /**
     * Sets a timestamp expiry for the inserted column.
     * This value is set in seconds.
     * @param expiry The expiry time.
     * @return
     */
    final def ttl(expiry: Duration): InsertQuery[T, R] = {
      query.ttl(expiry.inSeconds)
    }
  }

  implicit class FinagleUpdateQuery[T <: CassandraTable[T, R], R](val query: UpdateQuery[T, R]) extends AnyVal {

    /**
     * Sets a timestamp expiry for the inserted column.
     * This value is set in seconds.
     * @param expiry The expiry time.
     * @return
     */
    final def ttl(expiry: Duration): UpdateQuery[T, R] = {
      query.ttl(expiry.inSeconds)
    }
  }


  implicit class FinagleSelectWhereQuery[T <: CassandraTable[T, _]](val query: SelectWhere[T, _]) extends AnyVal {
    def execute()(implicit session: Session): Future[ResultSet] =
      query.statementExecuteToFuture(query.qb)
  }

  implicit class FinagleDeleteWhereQuery[T <: CassandraTable[T, R], R](val query: DeleteWhere[T, R]) extends AnyVal {
    def execute()(implicit session: Session): Future[ResultSet] =
      query.statementExecuteToFuture(query.qb)
  }

  implicit class FinagleAssignmentsQuery[T <: CassandraTable[T, R], R](val query: AssignmentsQuery[T, R]) extends AnyVal {
    def execute()(implicit session: Session): Future[ResultSet] =
      query.statementExecuteToFuture(query.qb)
  }
}
