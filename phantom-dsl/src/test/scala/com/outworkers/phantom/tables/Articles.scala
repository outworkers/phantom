/*
 * Copyright 2013 - 2017 Outworkers Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.outworkers.phantom.tables

import com.outworkers.phantom.connectors.RootConnector
import com.outworkers.util.testing.sample
import com.outworkers.phantom.builder.query.InsertQuery
import com.outworkers.phantom.dsl._

case class Article(
  id: UUID,
  name: String,
  orderId: Long
)

sealed class Articles extends CassandraTable[ConcreteArticles, Article] {

  object id extends UUIDColumn(this) with PartitionKey
  object name extends StringColumn(this)
  object orderId extends LongColumn(this)

  override def fromRow(row: Row): Article = Article(id(row), name(row), orderId(row))
}

abstract class ConcreteArticles extends Articles with RootConnector {
  override def tableName: String = "articles"

  def store(article: Article): InsertQuery.Default[ConcreteArticles, Article] = {
    insert
      .value(_.id, article.id)
      .value(_.name, article.name)
      .value(_.orderId, article.orderId)
  }
}


sealed class ArticlesByAuthor extends CassandraTable[ConcreteArticlesByAuthor, Article] {

  object author_id extends UUIDColumn(this) with PartitionKey
  object category extends UUIDColumn(this) with PartitionKey
  object id extends UUIDColumn(this) with PrimaryKey

  object name extends StringColumn(this)
  object orderId extends LongColumn(this)

  override def fromRow(row: Row): Article = {
    Article(
      name = name(row),
      id = id(row),
      orderId = orderId(row)
    )
  }
}

abstract class ConcreteArticlesByAuthor extends ArticlesByAuthor with RootConnector {

  def store(author: UUID, category: UUID, article: Article): InsertQuery.Default[ConcreteArticlesByAuthor, Article] = {
    insert
      .value(_.author_id, author)
      .value(_.category, category)
      .value(_.id, article.id)
      .value(_.name, article.name)
      .value(_.orderId, article.orderId)
  }
}
