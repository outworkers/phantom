package net
package liftweb
package cassandra
package record

import scala.collection.JavaConverters._
import net.liftweb.record.{ MetaRecord }

trait CassandraMetaRecord[MyType <: CassandraRecord[MyType]] extends MetaRecord[MyType] {
  self: MyType =>

}
