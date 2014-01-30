package com.newzly.phantom.tables

import java.util.UUID
import com.datastax.driver.core.Row
import com.newzly.phantom.CassandraTable
import com.newzly.phantom.keys.{ LongOrderKey, PrimaryKey }
import com.newzly.phantom.helper.{
  ModelSampler,
  Sampler,
  TestSampler
}
import com.newzly.phantom.Implicits._

case class Article(
  name: String,
  id: UUID,
  order_id: Long
)

object Article extends ModelSampler[Article] {
  def sample: Article = Article(
    Sampler.getAUniqueString,
    UUID.randomUUID(),
    Sampler.getARandomInteger().toLong
  )
}

sealed class Articles private() extends CassandraTable[Articles, Article] with LongOrderKey[Articles, Article] {

  def meta = Articles
  object id extends UUIDColumn(this) with PrimaryKey
  object name extends StringColumn(this)

  override def fromRow(row: Row): Article = {
    Article(name(row), id(row), order_id(row))
  }
}

object Articles extends Articles with TestSampler[Articles, Article] {

  override def tableName = "articlestest"

}