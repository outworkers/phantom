package com.outworkers.phantom.macros.tables

import com.outworkers.phantom.dsl._
import com.outworkers.phantom.tables.Article
import com.outworkers.phantom.NamingStrategy.SnakeCase.caseSensitive

abstract class NamedArticlesByAuthor extends Table[NamedArticlesByAuthor, Article] {
  object id extends UUIDColumn with PartitionKey
  object name extends StringColumn
  object orderId extends LongColumn
}