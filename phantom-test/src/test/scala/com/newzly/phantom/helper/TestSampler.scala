package com.newzly.phantom.helper

import java.io.Serializable
import scala.Product

/**
 * A basic trait implemented by all test tables.
 * @tparam Row The case class type returned.
 */
trait TestSampler[Row] {

  /**
   * This must specify the schema expected to exist in the database
   * for the specific table.
   * @return
   */
  def createSchema: String
}

/**
 * A simple model sampler trait.
 * Forces implementing case class models to provide a way to sample themselves.
 * This can only be mixed into a case class or Product with Serializable implementor.
 */
trait ModelSampler {

  /**
   * The sample method. Using basic sampling, this will produce a unique sample
   * of the implementing class.
   * @return A unique sample of the class.
   */
  def sample: this.type
}
