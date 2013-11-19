package net.liftweb.cassandra.blackpepper

import scala.collection.JavaConverters._

import com.datastax.driver.core.Row

import net.liftweb.json.{ DefaultFormats, JsonAST, JsonDSL, Extraction }

trait AbstractColumn[T] extends CSWrites[T] {

  type ValueType

  def name: String

  def apply(r: Row): ValueType

  def optional(r: Row): Option[T]
}

trait Column[T] extends AbstractColumn[T] {

  type ValueType = T

  override def apply(r: Row): T =
    optional(r).getOrElse(throw new Exception(s"can't extract required value for column '$name'"))
}

trait OptionalColumn[T] extends AbstractColumn[T] {

  type ValueType = Option[T]

  override def apply(r: Row) = optional(r)
}

class OptionalPrimitiveColumn[T: CSPrimitive](val name: String) extends OptionalColumn[T] {

  def toCType(v: T): AnyRef = CSPrimitive[T].toCType(v)

  def optional(r: Row): Option[T] = implicitly[CSPrimitive[T]].fromRow(r, name)
}

class PrimitiveColumn[RR: CSPrimitive](val name: String) extends Column[RR] {

  def toCType(v: RR): AnyRef = CSPrimitive[RR].toCType(v)

  def optional(r: Row): Option[RR] =
    implicitly[CSPrimitive[RR]].fromRow(r, name)
}

class JsonTypeColumn[RR: Format](val name: String) extends Column[RR] {

  def toCType(v: RR): AnyRef = Json.stringify(Json.toJson(v))

  def optional(r: Row): Option[RR] = {
    Option(r.getString(name)).flatMap(e => Json.fromJson(Json.parse(e)).asOpt)
  }
}

class EnumColumn[EnumType <: Enumeration](enum: EnumType, val name: String) extends Column[EnumType#Value] {

  def toCType(v: EnumType#Value): AnyRef = v.toString

  def optional(r: Row): Option[EnumType#Value] =
    Option(r.getString(name)).flatMap(s => enum.values.find(_.toString == s))

}

class SeqColumn[RR: CSPrimitive](val name: String) extends Column[Seq[RR]] {

  def toCType(values: Seq[RR]): AnyRef = values.map(CSPrimitive[RR].toCType).asJava

  override def apply(r: Row): Seq[RR] = {
    optional(r).getOrElse(Seq.empty)
  }

  def optional(r: Row): Option[Seq[RR]] = {
    val i = implicitly[CSPrimitive[RR]]
    Option(r.getList(name, i.cls)).map(_.asScala.map(e => i.fromCType(e.asInstanceOf[AnyRef])).toIndexedSeq)
  }
}

class MapColumn[K: CSPrimitive, V: CSPrimitive](val name: String) extends Column[Map[K, V]] {

  def toCType(values: Map[K, V]): AnyRef = values.map { case (k, v) => CSPrimitive[K].toCType(k) -> CSPrimitive[V].toCType(v) }.asJava

  override def apply(r: Row): Map[K, V] = {
    optional(r).getOrElse(Map.empty)
  }

  def optional(r: Row): Option[Map[K, V]] = {
    val ki = implicitly[CSPrimitive[K]]
    val vi = implicitly[CSPrimitive[V]]
    Option(r.getMap(name, ki.cls, vi.cls)).map(_.asScala.map {
      case (k, v) =>
        ki.fromCType(k.asInstanceOf[AnyRef]) -> vi.fromCType(v.asInstanceOf[AnyRef])
    } toMap)
  }
}

class JsonTypeSeqColumn[RR: Format](val name: String) extends Column[Seq[RR]] {

  def toCType(values: Seq[RR]): AnyRef = values.map(v => Json.stringify(Json.toJson(v))).asJava

  override def apply(r: Row): Seq[RR] = {
    optional(r).getOrElse(Seq.empty)
  }

  def optional(r: Row): Option[Seq[RR]] = {
    Option(r.getList(name, classOf[String])).map(_.asScala.flatMap(e => Json.fromJson(Json.parse(e)).asOpt))
  }
}