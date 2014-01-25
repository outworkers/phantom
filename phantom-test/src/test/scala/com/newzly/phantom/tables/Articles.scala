package com.newzly.phantom.tables

import java.util.UUID
import com.datastax.driver.core.Row
import com.newzly.phantom.{ CassandraTable, PrimitiveColumn }
import com.newzly.phantom.field.{ LongOrderKey, UUIDPk }
import com.newzly.phantom.helper.{Sampler, TestSampler}


case class Article(
  name: String,
  id: UUID,
  order_id: Long
)

sealed class Articles private() extends CassandraTable[Articles, Article] with UUIDPk[Articles] with LongOrderKey[Articles] {
  object name extends PrimitiveColumn[String]
  override def fromRow(row: Row): Article = {
    Article(name(row), id(row), order_id(row))
  }
}

object Articles extends Articles with TestSampler[Article] {

  override def tableName = "articles"

  /**
   * Generate a unique article.
   * @param order The order index of the article.
   * @return A unique article.
   */
  def sample(order: Long = Sampler.getARandomInteger()): Article = {
    Article(
      Sampler.getAUniqueString,
      UUID.randomUUID(),
      order
    )
  }

  def createSchema: String = {
    ""
  }
}