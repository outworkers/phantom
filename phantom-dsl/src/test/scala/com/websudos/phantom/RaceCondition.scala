package com.websudos.phantom

import java.util.UUID
import java.util.concurrent.{Callable, Executors}

import scala.collection.JavaConversions._

import org.scalatest.FunSuite

import com.datastax.driver.core.Row
import com.websudos.phantom.Implicits._

class RaceCondition extends FunSuite {

  case class Group(id: UUID, name: String)

  class Groups extends CassandraTable[Groups, Group] {
    object id extends UUIDColumn(this) with PartitionKey[UUID]
    object name extends StringColumn(this)

    override def fromRow(row: Row) = Group(id(row), name(row))
  }

  case class User(id: UUID, name: String)

  class Users extends CassandraTable[Users, User] {
    object id extends UUIDColumn(this) with PartitionKey[UUID]
    object name extends StringColumn(this)

    override def fromRow(row: Row) = User(id(row), name(row))
  }

  test("parallel tables instantiation") {
    val executor = Executors.newFixedThreadPool(2)
    val futureResults = executor.invokeAll(List(
      new Callable[AnyRef] {
        override def call() = new Users
      },
      new Callable[AnyRef] {
        override def call() = new Groups
      }
    ))
    futureResults.map(_.get())
  }

  test("sequential tables instantiation") {
    new Groups
    new Users
  }
}
