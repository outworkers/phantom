package com.newzly.phantom.column

import com.newzly.phantom.CassandraTable
import org.apache.log4j.Logger
import com.datastax.driver.core.Row
import com.twitter.util.NonFatal

class JsonColumn[Owner <: CassandraTable[Owner, Record], Record, RR: Manifest](table: CassandraTable[Owner, Record]) extends Column[Owner, Record, RR](table) {

  val mf = implicitly[Manifest[RR]]
  val cassandraType = "text"

  def toCType(v: RR): AnyRef = {
    val s = JsonSerializer.serializeJson(v)
    table.logger.info(s)
    s
  }

  def optional(r: Row): Option[RR] = {
    try {
      val json = r.getString(name)
      table.logger.info(s"Attempting to de-serialize JSON: $json")
      Some(JsonSerializer.deserializeJson[RR](json))
    } catch{
      case NonFatal(e) => {
        table.logger.error(e.getMessage)
        None
      }
    }
  }
}
