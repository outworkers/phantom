package com.newzly.cassandra.phantom.dsl

import java.util.UUID

import org.scalatest.concurrent.ScalaFutures

import com.datastax.driver.core.Row
import com.newzly.cassandra.phantom.{PrimitiveColumn, CassandraTable}
import com.newzly.cassandra.phantom.field.{ UUIDPk, LongOrderKey }
import com.newzly.cassandra.phantom.query.SelectWhere._

class SkippingRecordsTest extends BaseTest with ScalaFutures {

  it should "allow skipping records " in {

    case class Article(val name: String, id: UUID, order_id: Long)
    class Articles extends CassandraTable[Articles, Article] with UUIDPk[Articles] with LongOrderKey[Articles] {

      object name extends PrimitiveColumn[String]

      override def fromRow(row: Row): Article = {
        Article(name(row), id(row), order_id(row))
      }
    }

    object Articles extends Articles {
      override val tableName = "articles"
    }

    Articles.select.skip(5)

  }

}
