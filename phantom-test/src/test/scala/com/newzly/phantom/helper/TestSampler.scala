package com.newzly.phantom.helper

import java.util.concurrent.atomic.AtomicBoolean
import org.apache.log4j.Logger
import com.datastax.driver.core.Session
import com.newzly.phantom.CassandraTable
import com.newzly.phantom.helper.AsyncAssertionsHelper._
/**
 * A basic trait implemented by all test tables.
 * @tparam Row The case class type returned.
 */
trait TestSampler[Owner <: CassandraTable[Owner, Row], Row] {
  self : CassandraTable[Owner, Row] =>

  /**
   * An Atomic boolean storing the insertion status of the schema.
   * This is used to prevent a table schema from being inserted twice.
   */
  private[this] val schemaCreated = new AtomicBoolean(false)


  /**
   * Inserts the schema into the database in a blocking way.
   * This is intentionally left without an else behaviour.
   * Throwing an error here is not recommended.
   * Cassandra will automatically throw an error if the schema is inserted more then once.
   * @param session The Cassandra session.
   *
   * ATTENTION!!! this method creates the schema in a sync mode, the unit tests rely on it to be synced
   */
  def insertSchema(implicit session: Session): Unit = {
    if (schemaCreated.compareAndSet(false, true)) {
      logger.info("Schema agreement in progress: ")
      create.execute().sync()
      logger.debug("Schema inserted")
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
