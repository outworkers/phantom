/*
 * Copyright 2013 - 2017 Outworkers Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.outworkers.phantom

import java.util.UUID

import org.joda.time.{DateTime, DateTimeZone}
import org.json4s.JsonAST.{JInt, JString}
import org.json4s.{Formats, JValue, MappingException, Serializer, TypeInfo}

import scala.util.control.NonFatal
import scala.util.{Failure, Success, Try}

trait JsonFormats {

  class UUIDSerializer extends Serializer[UUID] {
    private[this] val Class = classOf[UUID]

    def deserialize(implicit format: Formats): PartialFunction[(TypeInfo, JValue), UUID] = {
      case (TypeInfo(Class, _), json) => json match {
        case JString(value) => try {
          UUID.fromString(value)
        }  catch {
          case NonFatal(err) =>
            val exception = new MappingException(s"Couldn't extract an UUID from $value")
            exception.initCause(err)
            throw exception
        }
        case x => throw new MappingException("Can't convert " + x + " to UUID")
      }
    }

    def serialize(implicit format: Formats): PartialFunction[Any, JValue] = {
      case x: UUID => JString(x.toString)
    }
  }

  sealed class DateTimeSerializer extends Serializer[DateTime] {

    val DateTimeClass = classOf[DateTime]

    def deserialize(implicit format: Formats): PartialFunction[(TypeInfo, JValue), DateTime] = {
      case (TypeInfo(DateTimeClass, _), json) => json match {
        case JString(value) =>
          Try {
            new DateTime(value.toLong, DateTimeZone.UTC)
          } match {
            case Success(dt) => dt
            case Failure(err) => {
              val exception = new MappingException(s"Couldn't extract a DateTime from $value")
              exception.initCause(err)
              throw exception
            }
          }
        case JInt(value) =>
          Try(new DateTime(value.toLong, DateTimeZone.UTC)) match {
            case Success(dt) => dt
            case Failure(err) =>
              val exception = new MappingException(s"Couldn't extract a DateTime from $value")
              exception.initCause(err)
              throw exception
          }
        case x => throw new MappingException("Can't convert " + x + " to DateTime")
      }
    }

    def serialize(implicit format: Formats): PartialFunction[Any, JValue] = {
      case x: DateTime => JInt(x.getMillis)
    }
  }

}
