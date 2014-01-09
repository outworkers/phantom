package com.newzly.phantom.dsl.crud

import org.scalatest.FlatSpec
import com.datastax.driver.core.Row
import com.newzly.phantom._
import java.util.UUID
import com.newzly.phantom.field.UUIDPk


class SchemaTest extends FlatSpec {

  case class Article(name: String, id: UUID)
  class Articles extends CassandraTable[Articles, Article] with UUIDPk[Articles] {

    object name extends PrimitiveColumn[String]

    override def fromRow(r: Row): Article = {
      Article(name(r), id(r))
    }
  }

  object Articles extends Articles
  Console.println(Articles.schema)

}
