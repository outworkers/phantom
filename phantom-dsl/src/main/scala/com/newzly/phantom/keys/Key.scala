package com.newzly.phantom.keys

import java.util.Date
import org.joda.time.DateTime
import com.newzly.phantom.CassandraTable
import com.newzly.phantom.column.{Keys, TimeSeries, Column}
import com.newzly.phantom.Implicits._

/**
 * A trait mixable into Column definitions to allow storing them as keys.
 */
trait Key{
  self: Keys=>
  override val isKey = true
}

