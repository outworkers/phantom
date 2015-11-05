package com.websudos.phantom.builder.query

import com.websudos.phantom.Manager
import com.websudos.phantom.builder.query.CQLQuery._

import java.net.InetAddress
import java.text.SimpleDateFormat

import org.joda.time.DateTime
import org.joda.time.format._

import org.json4s.Extraction._
import org.json4s.jackson.JsonMethods
import org.json4s.jackson.Serialization._
import org.json4s.{Formats, _}

import scala.math.BigInt
import scala.reflect.runtime.{universe => ru}
import scala.reflect.runtime.universe._
import scala.reflect.Manifest

/**
 * Created by rbolen on 10/29/15.
 */
object JsonUtils {
  val defaultFormats = new DefaultFormats {
    override def dateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS")
  }
  implicit val formats = defaultFormats + InetAddressSerializer + UuidSerializer + ScalaBigIntSerializer + JodaDateTimeSerializer
}

trait JsonUtils {
  def getRecord[Record](json: String, formats: Formats = JsonUtils.formats)(implicit mf: Manifest[Record]): Record = {
    Manager.logger.debug(s"getRecord called for json => ${json}")

    val jObject = JsonMethods.parse(json).asInstanceOf[JObject] // Chomp the 'Row[' and ']' off the string

    Manager.logger.debug(s"jObject parsed from json ${jObject}")

    val unwrappedJObject = unwrapJObject(jObject)

    Manager.logger.debug(s"modified tuples ${unwrappedJObject}")

//    implicitly[TypeTag[Record]]

    unwrappedJObject.extract(formats, mf)
  }

  def unwrapJObject(map: Map[String, Any]): JObject = {
    Manager.logger.debug(s"unwrapJObject (from a map) called with map ${map}")
    val modifiedTuples = map.filter(x => {
      Option(x._2).isDefined
    }).map(x => {
      Manager.logger.debug(s"unwrapping ${x} with type ${x._2.getClass().getCanonicalName()}")
      x._2 match {
        case jo: JObject => (unwrap(x._1), unwrapJObject(jo))
        case ja: JArray => (unwrap(x._1), unwrapJArray(ja))
        case js: JString => (unwrap(x._1), js)
        case ji: JInt => (unwrap(x._1), ji)
        case jd: JDouble => (unwrap(x._1), jd)
        case jb: JBool => (unwrap(x._1), jb)
        case m: Map[String, JValue] => (unwrap(x._1), unwrapJObject(m))
        case l: List[Any] => (unwrap(x._1), unwrapJArray(l))
        case s: String => (unwrap(x._1), JString(s))
        case bi: BigInt => (unwrap(x._1), JInt(bi))
        case d: Double => (unwrap(x._1), JDouble(d))
        case b: Boolean => (unwrap(x._1), JBool(b))
        case default => {
          Manager.logger.debug(s"Unwrapping default value from Map ${x._1} => ${default}")
          Manager.logger.debug(s"with type ${default.getClass().getCanonicalName()}")

          (unwrap(x._1), x._2)
        }
      }
    }).toList.map(x => (x._1, x._2.asInstanceOf[JValue]))

    JObject(modifiedTuples)
  }

  def unwrapJObject(jObject: JObject): JObject = {
    Manager.logger.debug(s"unwrapJObject called with JObject ${jObject}")
    val modifiedTuples = jObject.obj.map(x => {
      x._2 match {
        case jo: JObject => (unwrap(x._1), unwrapJObject(jo))
        case ja: JArray => (unwrap(x._1), unwrapJArray(ja))
        case js: JString => (unwrap(x._1), js)
        case ji: JInt => (unwrap(x._1), ji)
        case jd: JDouble => (unwrap(x._1), jd)
        case jb: JBool => (unwrap(x._1), jb)
        case m: Map[String, JValue] => (unwrap(x._1), unwrapJObject(m))
        case l: List[Any] => (unwrap(x._1), unwrapJArray(l))
        case default => {
          Manager.logger.debug(s"Unwrapping default value from JObject ${x._1} => ${default}")
          if (default != null) Manager.logger.debug(s"with type ${default.getClass().getCanonicalName()}")
          (unwrap(x._1), x._2)
        }
      }
    })

    JObject(modifiedTuples)
  }

  def unwrapJArray(jArray: JArray): JArray = {
    val list: List[Any] = jArray.values
    val modifiedList = list.map((value) => {
      value match {
        case jo: JObject => unwrapJObject(jo)
        case ja: JArray => unwrapJArray(ja)
        case js: JString => js
        case ji: JInt => ji
        case jd: JDouble => jd
        case jb: JBool => jb
        case bi: BigInt => JInt(bi)
        case m: Map[String, JValue] => unwrapJObject(m)
        case l: List[Any] => unwrapJArray(l)
        case s: String => JString(s)
        case bi: BigInt => JInt(bi)
        case d: Double => JDouble(d)
        case b: Boolean => JBool(b)
        case default => {
          Manager.logger.debug(s"unwrapping default value from JArray => ${default}")
          if (default != null) Manager.logger.debug(s"with type ${default.getClass().getCanonicalName()}")
          default
        }
      }
    })

    JArray(modifiedList.map(x => x.asInstanceOf[JValue]))
  }

  def unwrapJArray(list: List[Any]): JArray = {
    val modifiedList = list.map((value) => {
      value match {
        case jo: JObject => unwrapJObject(jo)
        case ja: JArray => unwrapJArray(ja)
        case js: JString => js
        case ji: JInt => ji
        case jd: JDouble => jd
        case jb: JBool => jb
        case bi: BigInt => JInt(bi)
        case m: Map[String, JValue] => unwrapJObject(m)
        case l: List[Any] => unwrapJArray(l)
        case s: String => JString(s)
        case bi: BigInt => JInt(bi)
        case d: Double => JDouble(d)
        case b: Boolean => JBool(b)
        case default => {
          Manager.logger.debug(s"unwrapping default value from JArray => ${default}")
          if (default != null) Manager.logger.debug(s"with type ${default.getClass().getCanonicalName()}")
          default
        }
      }
    })

    JArray(modifiedList.map(x => x.asInstanceOf[JValue]))
  }

  def unwrap(s: String): String = {
    if (s.startsWith( """"\"""")) s.toString().substring(3, s.length - 3)
    else if (s.startsWith("\"")) s.toString().substring(1, s.length - 1)
    else s.toString()
  }

  def parseToJObject(json: String): JObject = JsonMethods.parse(json).asInstanceOf[JObject]

  def writeToString(jObject: JObject, formats: Formats = JsonUtils.formats): String = {
    write(jObject)(formats)
  }

  def modifyJObject(jObject: JObject): JObject = {
    Manager.logger.debug(s"modifyJObject ${jObject}")

    val modifiedTuples = jObject.obj.map(x => {
      Manager.logger.debug(s"JObject value ${x}, ${x.getClass().getCanonicalName()}")
      (CQLQuery.escapeDoubleQuotes(x._1), CQLQuery.recurseAndEscapeDoubleQuote(x._2).asInstanceOf[JValue])
    })

    JObject(modifiedTuples)
  }

  def modifyMap(map: Map[String, Any]): JObject = {
    Manager.logger.debug(s"modifyMap ${map}")

    val modifiedTuples = map.map(x => {
      val jVal = x._2 match {
        case s: String => JString(s)
        case i: BigInt => JInt(i.longValue())
        case d: Double => JDouble(d)
        case b: Boolean => JBool(b)
        case a: List[Any] => modifyList(a)
        case jo: JObject => modifyJObject(jo)
        case m: Map[String, JValue] => modifyMap(m)
        case ja: JArray => modifyJArray(ja)
        case js: JString => js
        case ji: JInt => ji
        case jd: JDouble => jd
        case jb: JBool => jb
        case default => {
          Manager.logger.debug(s"Found default value ${default}")
          if (default != null) Manager.logger.debug(s"with type ${default.getClass().getCanonicalName()}")
          JString(default.toString())
        }
      }
      Manager.logger.debug(s"JObject value ${x}, ${x.getClass().getCanonicalName()}")
      (CQLQuery.escapeDoubleQuotes(x._1), CQLQuery.recurseAndEscapeDoubleQuote(jVal))
    })

    JObject(modifiedTuples.toList)
  }

  def modifyList(list: List[Any]): JArray = {
    Manager.logger.debug(s"modifyJArray => ${list}")

    val modifiedArray = list.map((value) => {
      Manager.logger.debug(s"JArray value => ${value}, ${value.getClass().getCanonicalName()}")
      value match {
        case jo: JObject => modifyJObject(jo)
        case ja: JArray => modifyJArray(ja)
        case m: Map[String, JValue] => modifyMap(m)
        case l: List[Any] => modifyList(l)
        case js: JString => js
        case ji: JInt => ji
        case jd: JDouble => jd
        case jb: JBool => jb
        case bi: BigInt => JInt(bi.longValue())
        case d: Double => JDouble(d)
        case s: String => JString(s)
        case b: Boolean => JBool(b)
        case default => {
          Manager.logger.debug(s"Non JValue found => ${default}")
          if (default != null) Manager.logger.debug(s"with type ${default.getClass().getCanonicalName()}")
          JString(default.toString())
        }
      }
    })

    JArray(modifiedArray.map(x => x.asInstanceOf[JValue]))
  }

  def modifyJArray(array: JArray): JArray = {
    Manager.logger.debug(s"modifyJArray => ${array.values}")
    modifyList(array.values)
  }

  def recurseAndEscapeDoubleQuote(jValue: JValue): JValue = {
    Manager.logger.debug(s"recurseAndEscapeDoubleQuote called on JValue => ${jValue} with type ${jValue.getClass().getCanonicalName()}")
    jValue match {
      case jo: JObject => {
        modifyJObject(jo)
      }
      case array: JArray => {
        modifyJArray(array)
      }
      case js: JString => js
      case default => {
        Manager.logger.debug(s"Default case => ${default}")
        if (default != null) Manager.logger.debug(s"with type ${default.getClass().getCanonicalName()}")
        default
      }
    }
  }
}

case object JodaDateTimeSerializer extends CustomSerializer[org.joda.time.DateTime](format => ( {
  case JNull => null
  case JString(s) => {
    DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss.SSS").parseDateTime(s)
  }
}, {
  case d: DateTime => JString(d.toString())
}))

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

case object ScalaBigIntSerializer extends CustomSerializer[BigInt](format => ( {
  case ji: JInt => {
    Manager.logger.debug(s"Converting JInt to BigInt ${ji} => ${ji.values}")
    ji.values
  }
}, {
  case bi: BigInt => {
    Manager.logger.debug(s"Converting BigInt to JInt ${bi} => ${bi.longValue()}")
    JInt(bi.longValue())
  }
}))
