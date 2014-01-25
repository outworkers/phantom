package com.newzly.phantom.tables

import java.util.UUID
import com.datastax.driver.core.Row
import com.newzly.phantom.{ CassandraTable, PrimitiveColumn }
import com.newzly.phantom.field.{ LongOrderKey, UUIDPk }
import com.newzly.phantom.helper.{
  ModelSampler,
  Sampler,
  TestSampler
}

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

sealed class Articles private() extends CassandraTable[Articles, Article] with UUIDPk[Articles] with LongOrderKey[Articles] {
  object name extends PrimitiveColumn[String]
  override def fromRow(row: Row): Article = {
    Article(name(row), id(row), order_id(row))
  }
}

object Articles extends Articles with TestSampler[Article] {

  override def tableName = "articlestest"

  def createSchema: String = {
    ""
  }
}