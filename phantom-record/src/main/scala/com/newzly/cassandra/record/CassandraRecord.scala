package com.newzly.cassandra.record

import net.liftweb.record.{ MetaRecord, Record }

trait CassandraRecord[MyType <: CassandraRecord[MyType]] extends Record[MyType] {
  self: MyType =>

  def save: Unit = {
    runSafe {

    }
  }

}