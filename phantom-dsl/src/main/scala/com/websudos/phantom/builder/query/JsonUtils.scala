package com.websudos.phantom.builder.query

import com.websudos.phantom.Manager

import java.net.InetAddress
import java.text.SimpleDateFormat

import org.json4s.jackson.JsonMethods
import org.json4s.jackson.Serialization._
import org.json4s.{Formats, _}

import scala.reflect.runtime.{universe => ru}
import scala.reflect.runtime.universe._
import scala.reflect.Manifest

/**
 * Created by rbolen on 10/29/15.
 */
trait JsonUtils {

  val defaultFormats = new DefaultFormats {
    override def dateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS")
  }
  implicit val formats = defaultFormats + InetAddressSerializer + UuidSerializer

  def getRecord[Record](json: String)(implicit mf: Manifest[Record]): Record = {
    Manager.logger.debug(s"getRecord called for json => ${json}")
    val jObject = JsonMethods.parse(json.substring(4, json.length - 1)).asInstanceOf[JObject] // Chomp the 'Row[' and ']' off the string

    Manager.logger.debug(s"jObject parsed from json ${jObject}")

    val modifiedTuples = jObject.obj.map(x => (unwrap(x._1), x._2))

    implicitly[TypeTag[Record]]

    JObject(modifiedTuples).extract[Record]
  }

  def unwrap(s: String): String = {
    if (s.startsWith("\""))
      s.toString().substring(1, s.length - 1)
    else
      s.toString()
  }

  def parseToJObject(json: String): JObject = JsonMethods.parse(json).asInstanceOf[JObject]

  def writeToString(jObject: JObject): String = write(jObject)

}

case object InetAddressSerializer extends CustomSerializer[java.net.InetAddress](format => ( {
  case JString(s) => InetAddress.getByName(s)
}, {
  case i: InetAddress => JString(i.getHostAddress())
}))

case object UuidSerializer extends CustomSerializer[java.util.UUID](format => ( {
  case JString(s) => java.util.UUID.fromString(s)
}, {
  case uuid: java.util.UUID => JString(uuid.toString())
}))
