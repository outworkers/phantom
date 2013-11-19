package net.liftweb.cassandra.blackpepper

import com.datastax.driver.core.Row
import java.util.{ UUID, Date }

trait CassandraWrites[T] {

  def toCType(v: T): AnyRef
}

trait CassandraPrimitive[T] extends CassandraWrites[T] {

  def cls: Class[_]
  def toCType(v: T): AnyRef = v.asInstanceOf[AnyRef]
  def fromCType(c: AnyRef): T = c.asInstanceOf[T]
  def fromRow(row: Row, name: String): Option[T]
}

object CassandraPrimitive {

  def apply[T: CassandraPrimitive]: CassandraPrimitive[T] = implicitly[CassandraPrimitive[T]]

  implicit object IntIsCassandraPrimitive extends CassandraPrimitive[Int] {

    def cls: Class[_] = classOf[java.lang.Integer]
    def fromRow(row: Row, name: String): Option[Int] = Option(row.getInt(name))
  }

  implicit object StringIsCassandraPrimitive extends CassandraPrimitive[String] {

    def cls: Class[_] = classOf[java.lang.String]
    def fromRow(row: Row, name: String): Option[String] = Option(row.getString(name))
  }

  implicit object DoubleIsCassandraPrimitive extends CassandraPrimitive[Double] {

    def cls: Class[_] = classOf[java.lang.Double]
    def fromRow(row: Row, name: String): Option[Double] = Option(row.getDouble(name))
  }

  implicit object DateIsCassandraPrimitive extends CassandraPrimitive[Date] {

    def cls: Class[_] = classOf[Date]
    def fromRow(row: Row, name: String): Option[Date] = Option(row.getDate(name))
  }

  implicit object BooleanIsCassandraPrimitive extends CassandraPrimitive[Boolean] {

    def cls: Class[_] = classOf[Boolean]
    def fromRow(row: Row, name: String): Option[Boolean] = Option(row.getBool(name))
  }

  implicit object UUIDIsCassandraPrimitive extends CassandraPrimitive[UUID] {

    def cls: Class[_] = classOf[UUID]
    def fromRow(row: Row, name: String): Option[UUID] = Option(row.getUUID(name))
  }
}