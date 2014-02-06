package com.newzly.phantom.dsl

import java.util.UUID
import com.datastax.driver.core.{ Session, Row }
import com.datastax.driver.core.utils.UUIDs
import org.scalatest.time.SpanSugar._
import com.newzly.phantom.Implicits._
import com.newzly.phantom.helper.{ BaseTest }
import com.newzly.phantom.helper.AsyncAssertionsHelper._
import org.scalatest.Assertions
import org.scalatest.concurrent.{PatienceConfiguration, AsyncAssertions}
import com.newzly.phantom.tables.{Article, Articles}

class SkipRecordsByToken extends BaseTest with Assertions with AsyncAssertions {
  val keySpace: String = "SkippingRecordsByTokenTest"
  implicit val s: PatienceConfiguration.Timeout = timeout(10 seconds)
   //QueryBuilder.token
  //https://datastax-oss.atlassian.net/browse/JAVA-44
  it should "allow skipping records " in {
    Articles.insertSchema(session)
    val article1 = Article.sample
    val article2 = Article.sample
    val article3 = Article.sample
    val article4 = Article.sample

    val result = for {
      i1 <- Articles.insert
        .value(_.name, article1.name).value(_.id, article1.id)
        .value(_.order_id, article1.order_id)
        .execute()
      i2 <- Articles.insert
        .value(_.name, article2.name)
        .value(_.id, article2.id)
        .value(_.order_id, article2.order_id)
        .execute()
      i3 <- Articles.insert
        .value(_.name, article3.name)
        .value(_.id, article3.id)
        .value(_.order_id, article3.order_id)
        .execute()

      i4 <- Articles.insert
        .value(_.name, article4.name)
        .value(_.id, article4.id)
        .value(_.order_id, article4.order_id)
        .execute()
      one <- Articles.select.one
    } yield i4


    result successful {
      r => {
        assert(condition = true)
      }
    }
  }

}

/*it should "allow skipping records in a manually defined table" in {

    case class Article(val name: String, id: UUID, order_id: Long)
    class Articles extends CassandraTable[Articles, Article] with UUIDPk[Articles] with LongOrderKey[Articles] {
      object name extends PrimitiveColumn[String]
      override def fromRow(row: Row): Article = {
        Article(name(row), id(row), order_id(row))
      }
    }

    object Articles extends Articles {
      override val tableName = "articlestestskip1"
    }
    val articlesTable =
      """ CREATE TABLE articlestestskip1
        |( id uuid,
        |name text,
        |order_id bigint,
        |PRIMARY KEY (id, order_id));
      """.stripMargin
    session.execute(articlesTable)


    val id =  UUIDs.timeBased()
    val article1 = Article("test1", id, 1)
    val article2 = Article("test2", id, 2)
    val article3 = Article("test3", id, 3)

    val result = for {
      i1 <- Articles.insert
        .value(_.name, article1.name).value(_.id, article1.id)
        .value(_.order_id, article1.order_id)
        .execute()
      i2 <- Articles.insert
        .value(_.name, article2.name)
        .value(_.id, article2.id)
        .value(_.order_id, article2.order_id)
        .execute()
      i3 <- Articles.insert
        .value(_.name, article3.name)
        .value(_.id, article3.id)
        .value(_.order_id, article3.order_id)
        .execute()
      res <- Articles.select.where(token(_.id) eqs token(id)).one
    } yield (res)

    result successful {
      row => assert(row.get == article2)
    }
  }

  it should "allow skipping one record in a automated defined table" in {

    case class Article(val name: String, id: UUID, order_id: Long)
    class Articles extends CassandraTable[Articles, Article] with UUIDPk[Articles] with LongOrderKey[Articles] {
      object name extends PrimitiveColumn[String]
      override def fromRow(row: Row): Article = {
        Article(name(row), id(row), order_id(row))
      }
    }

    object Articles extends Articles {
      override val tableName = "articlestest"
    }
    val id =  UUIDs.timeBased()
    val article1 = Article("test1", id, 1)
    val article2 = Article("test2", id, 2)
    val article3 = Article("test3", id, 3)

    val result = for {
      created  <- Articles.create(_.id,_.name,_.order_id).execute
      i1 <- Articles.insert
        .value(_.name, article1.name).value(_.id, article1.id)
        .value(_.order_id, article1.order_id)
        .execute()
      i2 <- Articles.insert
        .value(_.name, article2.name)
        .value(_.id, article2.id)
        .value(_.order_id, article2.order_id)
        .execute()
      i3 <- Articles.insert
        .value(_.name, article3.name)
        .value(_.id, article3.id)
        .value(_.order_id, article3.order_id)
        .execute()
      res <- Articles.select.where(_.id eqs id).skip(1).one
    } yield (res)

    result successful {
      row => assert(row.get == article2)
    }
  }

  it should "allow skipping more records using allow filtering" in {

    case class Article(val name: String, id: UUID, order_id: Long)
    class Articles extends CassandraTable[Articles, Article] with UUIDPk[Articles] with LongOrderKey[Articles] {
      object name extends PrimitiveColumn[String]
      override def fromRow(row: Row): Article = {
        Article(name(row), id(row), order_id(row))
      }
    }

    object Articles extends Articles {
      override val tableName = "articlestest2"
    }
    val id =  UUIDs.timeBased()
    val article1 = Article("test1", id, 1)
    val article2 = Article("test2", id, 2)
    val article3 = Article("test3", id, 3)

    val result = for {
      created  <- Articles.create(_.id,_.name,_.order_id).execute
      i1 <- Articles.insert
        .value(_.name, article1.name).value(_.id, article1.id)
        .value(_.order_id, article1.order_id)
        .execute()
      i2 <- Articles.insert
        .value(_.name, article2.name)
        .value(_.id, article2.id)
        .value(_.order_id, article2.order_id)
        .execute()
      i3 <- Articles.insert
        .value(_.name, article3.name)
        .value(_.id, article3.id)
        .value(_.order_id, article3.order_id)
        .execute()
      res <- Articles.select.allowFiltering().where(_.id eqs id).skip(2).one
    } yield (res)

    result successful {
      row => assert(row.get == article3)
    }
  }
}
*/
