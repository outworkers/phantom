package com.newzly.phantom.dsl

import scala.concurrent.blocking
import org.scalatest.concurrent.PatienceConfiguration
import org.scalatest.time.SpanSugar._
import com.newzly.phantom.Implicits._
import com.newzly.phantom.tables.{ Article, Articles }
import com.newzly.util.testing.AsyncAssertionsHelper._
import com.newzly.util.testing.cassandra.BaseTest

class SkipRecordsByToken extends BaseTest {
  val keySpace: String = "SkippingRecordsByTokenTest"
  implicit val s: PatienceConfiguration.Timeout = timeout(10 seconds)

  override def beforeAll(): Unit = {
    blocking {
      super.beforeAll()
      Articles.insertSchema()
    }
  }

  it should "allow skipping records using gtToken" in {
    val article1 = Article.sample
    val article2 = Article.sample
    val article3 = Article.sample
    val article4 = Article.sample

    val result = for {
      i1 <- Articles.insert
        .value(_.name, article1.name).value(_.id, article1.id)
        .value(_.order_id, article1.order_id)
        .future()
      i2 <- Articles.insert
        .value(_.name, article2.name)
        .value(_.id, article2.id)
        .value(_.order_id, article2.order_id)
        .future()
      i3 <- Articles.insert
        .value(_.name, article3.name)
        .value(_.id, article3.id)
        .value(_.order_id, article3.order_id)
        .future()

      i4 <- Articles.insert
        .value(_.name, article4.name)
        .value(_.id, article4.id)
        .value(_.order_id, article4.order_id)
        .future()
      one <- Articles.select.one
      next <- Articles.select.where(_.id gtToken one.get.id ).fetch
    } yield next

    result successful {
      r => {
        info (s"got exactly ${r.size} records")
        r.size shouldEqual 3
      }
    }
  }

  ignore should "allow skipping records using eqsToken" in {
    val article1 = Article.sample
    val article2 = Article.sample
    val article3 = Article.sample
    val article4 = Article.sample

    val result = for {
      truncate <- Articles.truncate.future()
      i1 <- Articles.insert
        .value(_.name, article1.name).value(_.id, article1.id)
        .value(_.order_id, article1.order_id)
        .future()
      i2 <- Articles.insert
        .value(_.name, article2.name)
        .value(_.id, article2.id)
        .value(_.order_id, article2.order_id)
        .future()
      i3 <- Articles.insert
        .value(_.name, article3.name)
        .value(_.id, article3.id)
        .value(_.order_id, article3.order_id)
        .future()

      i4 <- Articles.insert
        .value(_.name, article4.name)
        .value(_.id, article4.id)
        .value(_.order_id, article4.order_id)
        .future()
      one <- Articles.select.one
      next <- Articles.select.where(_.id eqsToken one.get.id).fetch
    } yield next


    result successful {
      r => {
        info (s"got exactly ${r.size} records")
        r.size shouldEqual 3
      }
    }
  }

  ignore should "allow skipping records using gteToken" in {
    val article1 = Article.sample
    val article2 = Article.sample
    val article3 = Article.sample
    val article4 = Article.sample

    val result = for {
      truncate <- Articles.truncate.future()
      i1 <- Articles.insert
        .value(_.name, article1.name).value(_.id, article1.id)
        .value(_.order_id, article1.order_id)
        .future()
      i2 <- Articles.insert
        .value(_.name, article2.name)
        .value(_.id, article2.id)
        .value(_.order_id, article2.order_id)
        .future()
      i3 <- Articles.insert
        .value(_.name, article3.name)
        .value(_.id, article3.id)
        .value(_.order_id, article3.order_id)
        .future()

      i4 <- Articles.insert
        .value(_.name, article4.name)
        .value(_.id, article4.id)
        .value(_.order_id, article4.order_id)
        .future()
      next <- Articles.select.where(_.id gteToken article2.id).fetch
    } yield next


    result successful {
      r => {
        info(s"got exactly ${r.size} records")
        r.size shouldEqual 3
      }
    }
  }

  ignore should "allow skipping records using ltToken" in {
    val article1 = Article.sample
    val article2 = Article.sample
    val article3 = Article.sample
    val article4 = Article.sample

    val result = for {
      truncate <- Articles.truncate.future()
      i1 <- Articles.insert
        .value(_.name, article1.name).value(_.id, article1.id)
        .value(_.order_id, article1.order_id)
        .future()
      i2 <- Articles.insert
        .value(_.name, article2.name)
        .value(_.id, article2.id)
        .value(_.order_id, article2.order_id)
        .future()
      i3 <- Articles.insert
        .value(_.name, article3.name)
        .value(_.id, article3.id)
        .value(_.order_id, article3.order_id)
        .future()

      i4 <- Articles.insert
        .value(_.name, article4.name)
        .value(_.id, article4.id)
        .value(_.order_id, article4.order_id)
        .future()
      next <- Articles.select.where(_.id ltToken article4.id).fetch
    } yield next


    result successful {
      r => {
        info(s"got exactly ${r.size} records")
        r.size shouldEqual 3
        r(0).id shouldEqual article1.id
        r(1).id shouldEqual article2.id
        r(2).id shouldEqual article3.id
      }
    }
  }

  ignore should "allow skipping records using lteToken" in {
    val article1 = Article.sample
    val article2 = Article.sample
    val article3 = Article.sample
    val article4 = Article.sample

    val result = for {
      truncate <- Articles.truncate.future()
      i1 <- Articles.insert
        .value(_.name, article1.name).value(_.id, article1.id)
        .value(_.order_id, article1.order_id)
        .future()
      i2 <- Articles.insert
        .value(_.name, article2.name)
        .value(_.id, article2.id)
        .value(_.order_id, article2.order_id)
        .future()
      i3 <- Articles.insert
        .value(_.name, article3.name)
        .value(_.id, article3.id)
        .value(_.order_id, article3.order_id)
        .future()
      i4 <- Articles.insert
        .value(_.name, article4.name)
        .value(_.id, article4.id)
        .value(_.order_id, article4.order_id)
        .future()
      next <- Articles.select.where(_.id lteToken article4.id).fetch
    } yield next


    result successful {
      r => {
        info (s"got exactly ${r.size} records")
        r.size shouldEqual 1
      }
    }
  }

}
