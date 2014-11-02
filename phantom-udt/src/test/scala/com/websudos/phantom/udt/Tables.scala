package com.websudos.phantom.udt


import com.twitter.util.Future
import com.websudos.phantom.Implicits._
import com.websudos.phantom.zookeeper.SimpleCassandraConnector

case class TestRecord(id: UUID, name: String, address: TestFields.address.type)

trait Connector extends SimpleCassandraConnector {
  val keySpace = "phantom_udt"
}

object Connector extends Connector

sealed class TestFields extends CassandraTable[TestFields, TestRecord] {

  object id extends UUIDColumn(this) with PartitionKey[UUID]
  object name extends StringColumn(this)

  object address extends UDTColumn(this) {
    val connector = Connector
    object postCode extends StringField[TestFields, TestRecord, address.type](this)
    object street extends StringField[TestFields, TestRecord, address.type](this)
    object test extends IntField[TestFields, TestRecord, address.type](this)
  }

  def fromRow(row: Row): TestRecord = {
    TestRecord(
      id(row),
      name(row),
      address(row)
    )
  }
}

object TestFields extends TestFields with Connector {

  def getAddress(id: UUID): Future[Option[TestRecord]] = {
    select.where(_.id eqs id).get()
  }

}
