/*
 * Copyright 2013-2015 Websudos, Limited.
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * - Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 *
 * - Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *
 * - Explicit consent must be obtained from the copyright owner, Outworkers Limited before any redistribution is made.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package com.websudos.phantom.tables

import com.websudos.phantom.builder.query.InsertQuery
import com.websudos.phantom.dsl._

case class Article(
  name: String,
  id: UUID,
  order_id: Long
)

sealed class Articles extends CassandraTable[ConcreteArticles, Article] {

  object id extends UUIDColumn(this) with PartitionKey[UUID]
  object name extends StringColumn(this)
  object orderId extends LongColumn(this)

  override def fromRow(row: Row): Article = {
    Article(
      name = name(row),
      id = id(row),
      order_id = orderId(row)
    )
  }
}

abstract class ConcreteArticles extends Articles with RootConnector {
  override def tableName: String = "articles"

  def store(article: Article): InsertQuery.Default[ConcreteArticles, Article] = {
    insert.value(_.id, article.id)
      .value(_.name, article.name)
      .value(_.orderId, article.order_id)
  }
}


sealed class ArticlesByAuthor extends CassandraTable[ConcreteArticlesByAuthor, Article] {

  object author_id extends UUIDColumn(this) with PartitionKey[UUID]
  object category extends UUIDColumn(this) with PartitionKey[UUID]
  object id extends UUIDColumn(this) with PrimaryKey[UUID]

  object name extends StringColumn(this)
  object orderId extends LongColumn(this)

  override def fromRow(row: Row): Article = {
    Article(
      name = name(row),
      id = id(row),
      order_id = orderId(row)
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
      .value(_.orderId, article.order_id)
  }
}
