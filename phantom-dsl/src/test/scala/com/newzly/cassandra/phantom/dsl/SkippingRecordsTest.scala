package com.newzly.cassandra.phantom.dsl

import java.util.UUID

import org.scalatest.concurrent.ScalaFutures

import com.datastax.driver.core.Row
import com.newzly.cassandra.phantom.{PrimitiveColumn, CassandraTable}
import com.newzly.cassandra.phantom.field.{ UUIDPk, LongOrderKey }
import com.newzly.cassandra.phantom.query.SelectWhere._
import com.datastax.driver.core.utils.UUIDs

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

    val article1 = Article("test", UUIDs.timeBased(),  1);
    val article2 = Article("test2", UUIDs.timeBased(), 2);
    val article3 = Article("test3", UUIDs.timeBased(), 3);

    Articles.insert
      .value(_.name, article1.name).value(_.id, article1.id)
      .value(_.order_id, article1.order_id)
      .execute().sync()

    Articles.insert
      .value(_.name, article2.name)
      .value(_.id, article2.id)
      .value(_.order_id, article2.order_id)
      .execute().sync()

    Articles.insert
      .value(_.name, article3.name)
      .value(_.id, article3.id)
      .value(_.order_id, article3.order_id)
      .execute().sync()

    val result = Articles.select.skip(1).one

    whenReady(result) {
      row => assert(row.get === article2)
    }
  }

}
