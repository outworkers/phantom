package com.websudos.phantom.udt

import java.net.InetAddress
import com.websudos.phantom.CassandraPrimitive
import com.websudos.phantom.Implicits._

class Field[T : CassandraPrimitive] extends AbstractField[T]

class StringField extends Field[String]
class InetField extends Field[InetAddress]
class IntField extends Field[Int]
class DoubleField extends Field[Double]
class LongField extends Field[Long]
class BigIntField extends Field[BigInt]
class