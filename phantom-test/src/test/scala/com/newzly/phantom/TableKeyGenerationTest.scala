package com.newzly.phantom

import org.scalatest.{ FlatSpec, Matchers, ParallelTestExecution }
import com.newzly.phantom.tables.{
  TableWithCompositeKey,
  TableWithCompoundKey,
  TableWithNoKey,
  TableWithSingleKey
}

class TableKeyGenerationTest extends FlatSpec with Matchers with ParallelTestExecution {

  it should "correctly create a Compound key from a table with a single Partition key" in {
    TableWithSingleKey.defineTableKey() shouldEqual s"PRIMARY KEY (id)"
  }

  it should "correctly create a Compound key from a table with a single Partition key and one Primary key" in {

    TableWithCompoundKey.defineTableKey() shouldEqual s"PRIMARY KEY (id, second)"
  }

  it should "correctly create a Composite key from a table with a two Partition keys and one Primary key" in {
    TableWithCompositeKey.defineTableKey() shouldEqual s"PRIMARY KEY ((id, second_part), second)"
  }

  it should "throw an error if the schema has no PartitionKey" in {
    intercept[InvalidPrimaryKeyException] {
      TableWithNoKey.defineTableKey()
    }
  }

}
