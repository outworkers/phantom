package com.newzly.phantom.column

import com.newzly.phantom.CassandraTable
import org.apache.log4j.Logger
import com.datastax.driver.core.Row
import com.twitter.util.NonFatal

class JsonColumn[Owner <: CassandraTable[Owner, Record], Record, RR: Manifest](override val table: CassandraTable[Owner, Record]) extends Column[Owner, Record, RR](table) {

  val logger = Logger.getLogger("JsonTypeColumn")
  val mf = implicitly[Manifest[RR]]
  val cassandraType = "text"

  def toCType(v: RR): AnyRef = {
    val s = JsonSerializer.serializeJson(v)
    logger.info(s)
    s
  }

  def optional(r: Row): Option[RR] = {
    try {
      val json = r.getString(name)
      logger.info(s"Attempting to de-serialize JSON: $json")
      Some(JsonSerializer.deserializeJson[RR](json))
    } catch{
      case NonFatal(e) => {
        logger.error(e.getMessage)
        None
      }
    }
  }
}
