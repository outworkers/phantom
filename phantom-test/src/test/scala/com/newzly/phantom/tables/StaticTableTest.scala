package com.newzly.phantom.tables

import java.util.UUID
import com.datastax.driver.core.Row
import com.newzly.phantom.Implicits._
import com.newzly.phantom.helper.TestSampler

sealed class StaticTableTest extends CassandraTable[StaticTableTest, (UUID, String)] {

  object id extends UUIDColumn(this) with PartitionKey[UUID]
  object staticTest extends StringColumn(this) with StaticColumn[String]

  def fromRow(row: Row): (UUID, String) = {
    Tuple2(id(row), staticTest(row))
  }
}

object StaticTableTest extends StaticTableTest with TestSampler[StaticTableTest, (UUID, String)]