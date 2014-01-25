package com.newzly.phantom.helper

/**
 * A basic trait implemented by all test tables.
 * @tparam Row The case class type returned.
 */
trait TestSampler[Row] {

  /**
   * Implementing objects must specify a way to produce a sample.
   * @return
   */
  def sample: Row

  /**
   * This must specify the schema expected to exist in the database
   * for the specific table.
   * @return
   */
  def createSchema: String
}
