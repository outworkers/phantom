package com.newzly.phantom.tables

import com.newzly.phantom.CassandraTable
import com.datastax.driver.core.Row
import com.newzly.phantom.column.{DateTimeColumn, PrimitiveColumn}
import com.newzly.phantom.keys.{PrimaryKey, PartitionKey}
import com.newzly.phantom.Implicits._

class TwoKeys extends CassandraTable[TwoKeys, Option[TwoKeys]] {
  override def fromRow(r: Row): Option[TwoKeys] = None
  override val tableName = "AJ"
  object pkey extends StringColumn(this) with PartitionKey[String]
  object intColumn1 extends IntColumn(this) with PrimaryKey[Int]
  object intColumn2 extends IntColumn(this) with PrimaryKey[Int]
  object intColumn3 extends IntColumn(this) with PrimaryKey[Int]
  object intColumn4 extends IntColumn(this) with PrimaryKey[Int]
  object intColumn5 extends IntColumn(this) with PrimaryKey[Int]
  object intColumn6 extends IntColumn(this) with PrimaryKey[Int]
  object intColumn7 extends IntColumn(this) with PrimaryKey[Int]
  object timestamp8 extends DateTimeColumn(this)
}

object TwoKeys extends TwoKeys{}
