package com.newzly.phantom.column

import scala.collection.JavaConverters._
import scala.collection.breakOut
import com.newzly.phantom.CassandraTable
import com.datastax.driver.core.Row
import com.twitter.util.Try

class JsonSeqColumn[Owner <: CassandraTable[Owner, Record], Record, RR: Manifest](table: CassandraTable[Owner, Record]) extends Column[Owner, Record, Seq[RR]](table) with Helpers {

  val cassandraType = "list<text>"

  def toCType(values: Seq[RR]): AnyRef = {
    val json = values.map {
      item => {
        Serializer.serialize(item)
      }
    }(breakOut)
    json.toSeq.asJava
  }

  override def apply(r: Row): Seq[RR] = {
    optional(r).getOrElse(Seq.empty[RR])
  }

  def optional(r: Row): Option[Seq[RR]] = {
    val items = r.getList(name, classOf[String]).asScala.flatMap {
      item => Try {
       // Some(Serializer.deserializeJson[RR](item))
        None
      } getOrElse None
    }
    //items.toSeq.toOption
    Some(Seq.empty[RR])
  }
}
