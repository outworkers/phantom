package com.newzly.phantom.tables

import java.util.UUID
import com.datastax.driver.core.Row
import com.newzly.phantom.Implicits._

sealed class BasicTable extends CassandraTable[BasicTable, String] {

  object id extends UUIDColumn(this) with PartitionKey[UUID]
  object id2 extends UUIDColumn(this) with PrimaryKey[UUID]
  object id3 extends UUIDColumn(this) with PrimaryKey[UUID]
  object placeholder extends StringColumn(this)

  def fromRow(r: Row): String = {
    placeholder(r)
  }
}

object BasicTable extends BasicTable

sealed class ComplexCompoundKeyTable extends CassandraTable[ComplexCompoundKeyTable, String] {

  object id extends UUIDColumn(this) with PartitionKey[UUID]
  object id2 extends UUIDColumn(this) with PrimaryKey[UUID]
  object id3 extends UUIDColumn(this) with PrimaryKey[UUID]
  object id4 extends UUIDColumn(this) with PrimaryKey[UUID]
  object id5 extends UUIDColumn(this) with PrimaryKey[UUID]
  object id6 extends UUIDColumn(this) with PrimaryKey[UUID]
  object id7 extends UUIDColumn(this) with PrimaryKey[UUID]
  object id8 extends UUIDColumn(this) with PrimaryKey[UUID]
  object id9 extends UUIDColumn(this) with PrimaryKey[UUID]
  object placeholder extends StringColumn(this)

  def fromRow(r: Row): String = {
    placeholder(r)
  }
}

object ComplexCompoundKeyTable extends ComplexCompoundKeyTable

sealed class SimpleCompoundKeyTable extends CassandraTable[SimpleCompoundKeyTable, String] {

  object id extends UUIDColumn(this) with PartitionKey[UUID]
  object id2 extends UUIDColumn(this) with PrimaryKey[UUID]
  object id3 extends UUIDColumn(this) with PrimaryKey[UUID]
  object placeholder extends StringColumn(this)

  def fromRow(r: Row): String = {
    placeholder(r)
  }
}

object SimpleCompoundKeyTable extends SimpleCompoundKeyTable