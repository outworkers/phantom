package com.newzly.phantom

import java.util.UUID
import org.scalatest.{ FlatSpec, Matchers, ParallelTestExecution }
import com.datastax.driver.core.Row
import com.newzly.phantom.Implicits._

sealed class TestTable extends CassandraTable[TestTable, String] {

  object id extends UUIDColumn(this) with PartitionKey[UUID]
  object id2 extends UUIDColumn(this) with PrimaryKey[UUID]
  object id3 extends UUIDColumn(this) with PrimaryKey[UUID]
  object placeholder extends StringColumn(this)

  def fromRow(r: Row): String = {
    placeholder(r)
  }
}

object TestTable extends TestTable

sealed class TestTable1 extends CassandraTable[TestTable1, String] {

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

object TestTable1 extends TestTable1

sealed class TestTable2 extends CassandraTable[TestTable2, String] {

  object id extends UUIDColumn(this) with PartitionKey[UUID]
  object id2 extends UUIDColumn(this) with PrimaryKey[UUID]
  object id3 extends UUIDColumn(this) with PrimaryKey[UUID]
  object placeholder extends StringColumn(this)

  def fromRow(r: Row): String = {
    placeholder(r)
  }
}

object TestTable2 extends TestTable2

class BasicTableMethods extends FlatSpec with Matchers with ParallelTestExecution {

  it should "retrieve the correct number of columns in a simple table" in {
    TestTable.columns.length shouldEqual 4
  }

  it should "retrieve the correct number of columns in a big table" in {
    TestTable1.columns.length shouldEqual 10
  }

  it should "retrieve the correct number of primary keys for a table" in {
    TestTable.primaryKeys.length shouldEqual 2
    TestTable.partitionKeys.length shouldEqual 1
  }
}
