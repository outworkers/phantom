package com.newzly.phantom.keys

import java.util.Date
import org.joda.time.DateTime
import com.newzly.phantom.CassandraTable
import com.newzly.phantom.column.{Keys, TimeSeries, Column}
import com.newzly.phantom.Implicits._

/**
 * A trait mixable into Column definitions to allow storing them as keys.
 */
trait SecondaryKey{
  self: Keys=>
  if (isPrimary) throw new Exception("Incompatible Keys")
  override val isSecondaryKey = true
}

