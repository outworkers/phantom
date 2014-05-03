package com.newzly.phantom.tables

import java.util.UUID
import com.datastax.driver.core.Row
import com.newzly.phantom.Implicits._
import com.newzly.phantom.helper.{ModelSampler, TestSampler}
import com.newzly.util.testing.Sampler
import com.datastax.driver.core.utils.UUIDs


case class SecondaryIndexRecord(primary: UUID, secondary: UUID, name: String)

object SecondaryIndexRecord extends ModelSampler[SecondaryIndexRecord] {
  def sample: SecondaryIndexRecord = SecondaryIndexRecord(
    UUIDs.timeBased(),
    UUIDs.timeBased(),
    Sampler.getARandomString
  )
}

sealed class SecondaryIndexTable extends CassandraTable[SecondaryIndexTable, SecondaryIndexRecord] {
  
  object id extends UUIDColumn(this) with PartitionKey[UUID]
  object secondary extends UUIDColumn(this) with Index[UUID]
  object name extends StringColumn(this)
  
  def fromRow(r: Row): SecondaryIndexRecord = SecondaryIndexRecord(
    id(r),
    secondary(r),
    name(r)
  )
}

object SecondaryIndexTable extends SecondaryIndexTable with TestSampler[SecondaryIndexTable, SecondaryIndexRecord]
