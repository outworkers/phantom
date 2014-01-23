/*
 * Copyright 2013 newzly ltd.
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
package com.newzly.phantom

import java.util.{ Map => JMap, UUID }
import scala.annotation.implicitNotFound
import scala.collection.breakOut
import scala.collection.JavaConverters._

import org.apache.log4j.Logger

import com.datastax.driver.core.Row
import com.datastax.driver.core.querybuilder.QueryBuilder

import com.fasterxml.jackson.core.`type`.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.fasterxml.jackson.module.scala.experimental.ScalaObjectMapper
import com.newzly.phantom.query.QueryCondition
import com.twitter.util.Try

object JsonSerializer {
  val mapper = new ObjectMapper() with ScalaObjectMapper
  mapper.registerModule(DefaultScalaModule)


  def deserializeJson[T: Manifest](value: String): T = mapper.readValue(value, typeReference[T](implicitly[Manifest[T]]))
  def serializeJson(value: Any): String = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(value)

  private[this] def typeReference[T: Manifest] = new TypeReference[T] {}
}

trait Helpers {
  private[phantom] implicit class RichSeq[T](val l: Seq[T]) {
    final def toOption: Option[Seq[T]] = if (l.isEmpty) None else Some(l)
  }
}

trait AbstractColumn[T] extends CassandraWrites[T] {

  type ValueType

  lazy val name: String = getClass.getSimpleName.replaceAll("\\$+", "").replaceAll("(anonfun\\d+.+\\d+)|", "")

  def apply(r: Row): ValueType

  def optional(r: Row): Option[T]
}

trait Column[T] extends AbstractColumn[T] {

  type ValueType = T

  override def apply(r: Row): T =
    optional(r).getOrElse(throw new Exception(s"can't extract required value for column '$name'"))

  def eqs (value: T): QueryCondition = {
    QueryCondition(QueryBuilder.eq(this.name, this.toCType(value)))
  }

  def lt (value: T): QueryCondition = {
    QueryCondition(QueryBuilder.lt(this.name, this.toCType(value)))
  }

  def gt (value: T): QueryCondition = {
    QueryCondition(QueryBuilder.gt(this.name, this.toCType(value)))
  }
 
}

trait OptionalColumn[T] extends AbstractColumn[T] {

  type ValueType = Option[T]

  override def apply(r: Row) = optional(r)
}

@implicitNotFound(msg = "Type ${RR} must be a Cassandra primitive")
class OptionalPrimitiveColumn[T: CassandraPrimitive] extends OptionalColumn[T] {

  def toCType(v: T): AnyRef = CassandraPrimitive[T].toCType(v)
  def cassandraType: String = CassandraPrimitive[T].cassandraType
  def optional(r: Row): Option[T] = implicitly[CassandraPrimitive[T]].fromRow(r, name)
}

@implicitNotFound(msg = "Type ${RR} must be a Cassandra primitive")
class PrimitiveColumn[@specialized(Int, Double, Float, Long) RR: CassandraPrimitive] extends Column[RR] {

  def cassandraType: String = CassandraPrimitive[RR].cassandraType
  def toCType(v: RR): AnyRef = CassandraPrimitive[RR].toCType(v)

  def optional(r: Row): Option[RR] =
    implicitly[CassandraPrimitive[RR]].fromRow(r, name)
}

@implicitNotFound(msg = "Type ${EnumType#Value} must be a Cassandra primitive")
class EnumColumn[EnumType <: Enumeration](enum: EnumType) extends Column[EnumType#Value] {

  def toCType(v: EnumType#Value): AnyRef = v.toString
  def cassandraType: String = "???"
  def optional(r: Row): Option[EnumType#Value] =
    Option(r.getString(name)).flatMap(s => enum.values.find(_.toString == s))
}

@implicitNotFound(msg = "Type ${RR} must be a Cassandra primitive")
class SetColumn[RR : CassandraPrimitive] extends Column[Set[RR]] {

  val cassandraType = s"set<${CassandraPrimitive[RR].cassandraType}>"
  def toCType(values: Set[RR]): AnyRef = values.map(CassandraPrimitive[RR].toCType).asJava

  override def apply(r: Row): Set[RR] = {
    optional(r).getOrElse(Set.empty)
  }

  def optional(r: Row): Option[Set[RR]] = {

    val i = implicitly[CassandraPrimitive[RR]]
    Option(r.getSet(name, i.cls)).map(_.asScala.map(e => i.fromCType(e.asInstanceOf[AnyRef])).toSet)
  }
}

@implicitNotFound(msg = "Type ${RR} must be a Cassandra primitive")
class SeqColumnNew[S <: Seq[RR],RR:CassandraPrimitive] extends Column[Seq[RR]] {

  val cassandraType = s"list<${CassandraPrimitive[RR].cassandraType}>"
  def toCType(values: Seq[RR]): AnyRef = values.map(CassandraPrimitive[RR].toCType).asJava

  override def apply(r: Row): Seq[RR] = {
    optional(r).getOrElse(Seq.empty[RR])
  }

  def optional(r: Row): Option[Seq[RR]] = {

    val i = implicitly[CassandraPrimitive[RR]]
    Option(r.getList(name, i.cls)).map(_.asScala.map(e => i.fromCType(e.asInstanceOf[AnyRef])).toIndexedSeq)
  }
}

@implicitNotFound(msg = "Type ${RR} must be a Cassandra primitive")
class ListColumn[RR: CassandraPrimitive] extends Column[List[RR]] {
  val cassandraType = s"list<${CassandraPrimitive[RR].cassandraType}>"

  def toCType(values: List[RR]): AnyRef = values.map(CassandraPrimitive[RR].toCType).toSeq.asJava

  override def apply(r: Row): List[RR] = {
    optional(r).getOrElse(List.empty[RR])
  }

  def optional(r: Row): Option[List[RR]] = {
    val primitive = implicitly[CassandraPrimitive[RR]]
    Option(r.getList(name, primitive.cls).asScala.toList.map(el => primitive.fromCType(el.asInstanceOf[AnyRef])))
  }

}

@implicitNotFound(msg = "Type ${RR} must be a Cassandra primitive")
class SeqColumn[RR: CassandraPrimitive] extends Column[Seq[RR]] {

  val cassandraType = s"list<${CassandraPrimitive[RR].cassandraType}>"
  def toCType(values: Seq[RR]): AnyRef = values.map(CassandraPrimitive[RR].toCType).asJava

  override def apply(r: Row): Seq[RR] = {
    optional(r).getOrElse(Seq.empty)
  }

  def optional(r: Row): Option[Seq[RR]] = {
    val i = implicitly[CassandraPrimitive[RR]]
    Option(r.getList(name, i.cls)).map(_.asScala.map(e => i.fromCType(e.asInstanceOf[AnyRef])).toIndexedSeq)
  }
}

@implicitNotFound(msg = "Type ${K} and ${V} must be a Cassandra primitives")
class MapColumn[K: CassandraPrimitive, V: CassandraPrimitive] extends Column[Map[K, V]] {

  val cassandraType = s"map<${CassandraPrimitive[K].cassandraType}, ${CassandraPrimitive[V].cassandraType}>"
  def toCType(values: Map[K, V]): JMap[AnyRef, AnyRef] = values.map {
    case (k, v) => CassandraPrimitive[K].toCType(k) -> CassandraPrimitive[V].toCType(v)
  }.asJava

  override def apply(r: Row): Map[K, V] = {
    optional(r).getOrElse(Map.empty[K, V])
  }

  def optional(r: Row): Option[Map[K, V]] = {
    val ki = implicitly[CassandraPrimitive[K]]
    val vi = implicitly[CassandraPrimitive[V]]
    Option(r.getMap(name, ki.cls, vi.cls)).map(_.asScala.map {
      case (k, v) =>
        ki.fromCType(k.asInstanceOf[AnyRef]) -> vi.fromCType(v.asInstanceOf[AnyRef])
    }(breakOut) toMap)
  }
}


class JsonTypeColumn[RR: Manifest] extends Column[RR] {

  val logger = Logger.getLogger("JsonTypeColumn")
  val mf = implicitly[Manifest[RR]]
  val cassandraType = "text"

  def toCType(v: RR): AnyRef = {
    val s = JsonSerializer.serializeJson(v)
    logger.warn(s)
    s
  }

  def optional(r: Row): Option[RR] = {
    Try {
      Some(JsonSerializer.deserializeJson[RR](r.getString(name)))
    } getOrElse None
  }
}

class JsonTypeSeqColumn[RR: Manifest] extends Column[Seq[RR]] with Helpers {

  val cassandraType = "list<text>"
  def toCType(values: Seq[RR]): AnyRef = {
    val json = values.map {
      item => {
        JsonSerializer.serializeJson(item)
      }
    }(breakOut)
    Console.println(json)
    json.toSeq.asJava
  }

  override def apply(r: Row): Seq[RR] = {
    optional(r).getOrElse(Seq.empty[RR])
  }

  def optional(r: Row): Option[Seq[RR]] = {
    val items = r.getList(name, classOf[String]).asScala.map {
      item => JsonSerializer.deserializeJson[RR](item)
    }
    items.toSeq.toOption
  }
}