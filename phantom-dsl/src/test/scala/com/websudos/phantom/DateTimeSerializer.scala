package com.websudos.phantom

import java.util.UUID

import com.websudos.phantom.dsl._
import net.liftweb.json.JsonAST.{JString, JValue}
import net.liftweb.json.{Formats, MappingException, Serializer, TypeInfo}

import scala.util.control.NonFatal

class DateTimeSerializer extends Serializer[DateTime] {
  private val Class = classOf[UUID]

  def deserialize(implicit format: Formats): PartialFunction[(TypeInfo, JValue), DateTime] = {
    case (TypeInfo(Class, _), json) => json match {
      case JString(value) => try {
        DateTimeIsPrimitive.fromString(value)
      }  catch {
        case NonFatal(err) => {
          val exception =  new MappingException(s"Couldn't extract an UUID from $value")
          exception.initCause(err)
          throw exception
        }
      }
      case x => throw new MappingException("Can't convert " + x + " to UUID")
    }
  }

  def serialize(implicit format: Formats): PartialFunction[Any, JValue] = {
    case x: DateTime => JString(DateTimeIsPrimitive.asCql(x))
  }
}

