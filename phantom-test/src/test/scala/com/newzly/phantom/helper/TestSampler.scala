package com.newzly.phantom.helper

import com.datastax.driver.core.Session
import com.newzly.phantom.CassandraTable
import com.newzly.phantom.finagle.Implicits._
import com.newzly.phantom.helper.AsyncAssertionsHelper._

/**
 * A basic trait implemented by all test tables.
 * @tparam Row The case class type returned.
 */
trait TestSampler[Owner <: CassandraTable[Owner, Row], Row] {
  self : CassandraTable[Owner, Row] =>

  /**
   * Inserts the schema into the database in a blocking way.
   * This is done with a try catch in order to avoid tests issues when the same keyspace is used
   * and schema is inserted twice
   * @param session The Cassandra session.
   *
   * ATTENTION!!! this method creates the schema in a sync mode, the unit tests rely on it to be synced
   */
  def insertSchema(implicit session: Session): Unit = {
      logger.info("Schema agreement in progress: ")
      try {
        create.future().value
      } catch {
        case e: Throwable =>
          logger.error(s"schema for ${this.tableName} could not be created. ")
          logger.error(e)
      }
  }
}

/**
 * A simple model sampler trait.
 * Forces implementing case class models to provide a way to sample themselves.
 * This can only be mixed into a case class or Product with Serializable implementor.
 */
trait ModelSampler[Model] {

  /**
   * The sample method. Using basic sampling, this will produce a unique sample
   * of the implementing class.
   * @return A unique sample of the class.
   */
  def sample: Model
}
