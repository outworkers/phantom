package com.newzly.phantom.helper

import java.util.concurrent.atomic.AtomicBoolean
import org.apache.log4j.Logger
import com.datastax.driver.core.Session
import com.newzly.phantom.CassandraTable

/**
 * A basic trait implemented by all test tables.
 * @tparam Row The case class type returned.
 */
trait TestSampler[Owner <: CassandraTable[Owner, Row], Row] {
  self : CassandraTable[Owner, Row] =>

  /**
   * This must specify the schema expected to exist in the database
   * for the specific table.
   * @return
   */
  def createSchema: String

  /**
   * Inserts the schema into the database in a blocking way.
   * @param session The Cassandra session.
   */
  def insertSchema(session: Session): Unit = {
    logger.info(s"Schema inserted: ${schemaCreated.get()}" )
    if (schemaCreated.compareAndSet(false,true)) {
      logger.info("Schema agreement in progress: " + createSchema)
      session.execute(createSchema)
      schemaCreated.set(true)
    } else throw new Exception("schema was already inserted")
  }

  override def create() = {
    throw new Exception("use TestSampler.insertSchema in tests to get the schema")
  }

  private[this] val schemaCreated = new AtomicBoolean(false)
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
