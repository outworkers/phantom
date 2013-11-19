package net.liftweb.cassandra.blackpepper.query

import scala.collection.JavaConverters._
import scala.concurrent.{ Future, ExecutionContext }

import com.datastax.driver.core.{ Row, ResultSet, Session, Statement }

import net.liftweb.cassandra.blackpepper.{ CassandraResultSetOperations, CassandraTable };

trait ExecutableStatement extends CassandraResultSetOperations {

  def qb: Statement

  def execute()(implicit session: Session, ec: ExecutionContext): Future[ResultSet] =
    session.executeAsync(qb)
}

trait ExecutableQuery[T <: CTable[T, _], R] extends CassandraResultSetOperations {

  def qb: Statement
  def table: CTable[T, _]
  def fromRow(r: Row): R

  def execute()(implicit session: Session, ec: ExecutionContext): Future[ResultSet] =
    session.executeAsync(qb)

  def fetchSync(implicit session: Session): Seq[R] = {
    session.execute(qb).all().asScala.toSeq.map(fromRow)
  }

  def fetch(implicit session: Session, ec: ExecutionContext): Future[Seq[R]] = {
    session.executeAsync(qb).map(_.all().asScala.toSeq.map(fromRow))
  }

  def one(implicit session: Session, ec: ExecutionContext): Future[Option[R]] = {
    session.executeAsync(qb).map(r => Option(r.one()).map(fromRow))
  }
}