package com.newzly.phantom

import java.util.UUID
import org.scalatest.{ FlatSpec, Matchers, ParallelTestExecution }
import com.datastax.driver.core.Row
import com.newzly.phantom.Implicits._

case class StubRecord(name: String, id: UUID)
sealed class TableWithSingleKey extends CassandraTable[TableWithSingleKey, StubRecord] {

  object id extends UUIDColumn(this) with PartitionKey[UUID]
  object name extends StringColumn(this)

  def fromRow(r: Row): StubRecord = {
    StubRecord(name(r), id(r))
  }
}

object TableWithSingleKey extends TableWithSingleKey

class TableWithCompoundKey extends CassandraTable[TableWithCompoundKey, StubRecord] {

  object id extends UUIDColumn(this) with PartitionKey[UUID]
  object second extends UUIDColumn(this) with PrimaryKey[UUID]
  object name extends StringColumn(this)

  def fromRow(r: Row): StubRecord = {
    StubRecord(name(r), id(r))
  }
}

object TableWithCompoundKey extends TableWithCompoundKey



sealed class TableWithCompositeKey extends CassandraTable[TableWithCompositeKey, StubRecord] {

  object id extends UUIDColumn(this) with PartitionKey[UUID]
  object second_part extends UUIDColumn(this) with PartitionKey[UUID]
  object second extends UUIDColumn(this) with PrimaryKey[UUID]
  object name extends StringColumn(this)

  def fromRow(r: Row): StubRecord = {
    StubRecord(name(r), id(r))
  }
}

object TableWithCompositeKey extends TableWithCompositeKey

sealed class TableWithNoKey extends CassandraTable[TableWithNoKey, StubRecord] {

  object id extends UUIDColumn(this)
  object name extends StringColumn(this)

  def fromRow(r: Row): StubRecord = {
    StubRecord(name(r), id(r))
  }
}

object TableWithNoKey extends TableWithNoKey


class TableKeyGenerationTest extends FlatSpec with Matchers with ParallelTestExecution {


  it should "correctly create a Compound key from a table with a single Partition key" in {


    TableWithSingleKey.defineTableKey() shouldEqual s"PRIMARY_KEY (id)"
  }

  it should "correctly create a Compound key from a table with a single Partition key and one Primary key" in {

    TableWithCompoundKey.defineTableKey() shouldEqual s"PRIMARY_KEY (id, second)"
  }

  it should "correctly create a Composite key from a table with a two Partition keys and one Primary key" in {
    TableWithCompoundKey.defineTableKey() shouldEqual s"PRIMARY_KEY ((id, second_part), second)"
  }


  it should "throw an error if the schema has no PartitionKey" in {
    intercept[InvalidPrimaryKeyException] {
      TableWithNoKey.defineTableKey()
    }
  }

}
