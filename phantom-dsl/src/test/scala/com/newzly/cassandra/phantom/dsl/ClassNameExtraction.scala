package com.newzly.cassandra.phantom.dsl

import java.net.InetAddress
import java.util.{ Date, UUID }

import scala.concurrent.{ Await, Future }
import scala.concurrent.duration.Duration
import scala.concurrent.ExecutionContext.Implicits.global

import com.datastax.driver.core.{ Session, Row }
import com.datastax.driver.core.utils.UUIDs

import com.newzly.cassandra.phantom._
import com.newzly.cassandra.phantom.CassandraTable

/**
 * Created by alex on 10/12/2013.
 */
class ClassNameExtraction extends BaseTest {

  implicit val session: Session = cassandraSession


  it should "correctly extract the table name" in {
    case class Table(name: String)
    class TestTable extends CassandraTable[TestTable, Table] {

      object name extends PrimitiveColumn[String]
      def fromRow(row: Row): Table = Table(name(row))
    }

    object TestTable extends TestTable

    assert(TestTable.name === "TableName")
  }


  it should "correctly extract the table name" in {
    case class Table(name: String)
    class Primitives extends CassandraTable[Primitives, Table] {

      object name extends PrimitiveColumn[String]

      def fromRow(row: Row): Table = Table(name(row))
     }

    object Primitives extends Primitives

    assert(Primitives.name === "Primitives")
  }

  it should "correctly extract the column names" in {
    case class Table(name: String)
    class TestTable extends CassandraTable[TestTable, Table] {

      object weirdname extends PrimitiveColumn[String]
      def fromRow(row: Row): Table = Table(weirdname(row))
    }

    object TestTable extends TestTable

    assert(TestTable.weirdname.name === "weirdname")
  }

}
