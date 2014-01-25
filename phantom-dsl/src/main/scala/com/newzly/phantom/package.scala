package com.newzly.cassandra

import com.datastax.driver.core.Row
import com.newzly.phantom.column.Column

package object phantom {

  type UUID = java.util.UUID
  type Date = java.util.Date

  type DateTime = org.joda.time.DateTime


  type Row = com.datastax.driver.core.Row
}
