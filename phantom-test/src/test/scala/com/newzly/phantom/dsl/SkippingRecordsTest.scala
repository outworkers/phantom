
package com.newzly.phantom.dsl

import org.scalatest.Assertions
import org.scalatest.concurrent.AsyncAssertions

import com.newzly.phantom.helper.BaseTest
import com.newzly.phantom.helper.AsyncAssertionsHelper._
import com.newzly.phantom.Implicits._
import com.newzly.phantom.tables.{ Article, Articles }


class SkippingRecordsTest extends BaseTest with Assertions with AsyncAssertions  {
  val keySpace: String = "SkippingRecordsTest"

  ignore should "allow skipping records " in {

    val article1 = Article.sample
    val article2 = Article.sample
    val article3 = Article.sample

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
      res <- Articles.select.skip(1).one
    } yield res

    result successful {
      row => assert(row.get == article2)
    }
  }

}

