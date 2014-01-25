package com.newzly.phantom.helper

import java.util.UUID
import scala.util.Random

object Sampler {

  /**
   * Returns a pseudo-random number between min and max, inclusive.
   * The difference between min and max can be at most
   * <code>Integer.MAX_VALUE - 1</code>.
   *
   * @param min Minimum value
   * @param max Maximum value.  Must be greater than min.
   * @return Integer between min and max, inclusive.
   * @see java.util.Random#nextInt(int)
   */
  def getARandomInteger(min: Int = 1, max: Int = Int.MaxValue): Int = {
    val rand = new Random()
    rand.nextInt((max - min) + 1) + min
  }

  /**
   * Get a unique random generated string.
   * This uses the default java GUID implementation.
   * @return A random string with 64 bits of randomness.
   */
  def getAUniqueString: String = {
    UUID.randomUUID().toString
  }
}
