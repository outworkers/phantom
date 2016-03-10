/*
 * Copyright 2013-2016 Websudos, Limited.
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
 * - Explicit consent must be obtained from the copyright owner, Websudos Limited before any redistribution is made.
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
package com.websudos.phantom.builder.query.prepared

import com.websudos.phantom.PhantomSuite
import com.websudos.phantom.dsl._
import com.websudos.phantom.tables._
import com.websudos.util.testing._
import org.slf4j.LoggerFactory

import scala.concurrent.Future

class PreparedRelationalOperatorQueryTest extends PhantomSuite {

  val logger = LoggerFactory.getLogger(this.getClass)

  val url = "index.html"

  val clicks = for (timestamp <- 1 to 100) yield {
    Click(url, timestamp)
  }

  override def beforeAll(): Unit = {
    super.beforeAll()

    database.clicks.insertSchema()

    val insertFutures = clicks.map { click =>
      database.clicks.store(click).future()
    }

    val futureInserts = Future.sequence(insertFutures)

    futureInserts.successful { inserts =>
      logger.debug(s"Added ${inserts.size} clicks")
    }
  }

  it should "support prepared statement with less than operator" in {
    val max: Long = 50

    val query = database.clicks.select.p_where(_.url eqs ?).p_and(_.timestamp < ?).prepare()

    val futureResults = query.bind(url, max).fetch()

    whenReady(futureResults) { results =>
      results.foreach { result =>
        result.url shouldEqual url
        result.timestamp should be < max
      }

      val lessThanClicks = clicks.filter(_.timestamp < max)

      lessThanClicks.foreach { click =>
        results should contain (click)
      }
    }
  }

  it should "support prepared statement with less than or equal operator" in {
    val max: Long = 40

    val query = database.clicks.select.p_where(_.url eqs ?).p_and(_.timestamp <= ?).prepare()

    val futureResults = query.bind(url, max).fetch()

    futureResults.successful { results =>
      results.foreach { result =>
        result.url shouldEqual url
        result.timestamp should be <= max
      }

      val lessThanOrEqualClicks = clicks.filter(_.timestamp <= max)

      lessThanOrEqualClicks.foreach { recipe =>
        results should contain (recipe)
      }
    }
  }

  it should "support prepared statement with greater than operator" in {
    val min: Long = 60

    val query = database.clicks.select.p_where(_.url eqs ?).p_and(_.timestamp > ?).prepare()

    val futureResults = query.bind(url, min).fetch()

    futureResults.successful { results =>
      results.foreach { result =>
        result.url shouldEqual url
        result.timestamp should be > min
      }

      val greaterThanClicks = clicks.filter(_.timestamp > min)

      greaterThanClicks.foreach { click =>
        results should contain (click)
      }
    }
  }

  it should "support prepared statement with greater than or equal operator" in {
    val min: Long = 75

    val query = database.clicks.select.p_where(_.url eqs ?).p_and(_.timestamp >= ?).prepare()

    val futureResults = query.bind(url, min).fetch()

    futureResults.successful { results =>
      results.foreach { result =>
        result.url shouldEqual url
        result.timestamp should be >= min
      }

      val greaterThanOrEqualClicks = clicks.filter(_.timestamp >= min)

      greaterThanOrEqualClicks.foreach { click =>
        results should contain (click)
      }
    }
  }

  it should "support prepared statement with less than and greater than operators" in {
    val min: Long = 10
    val max: Long = 40

    val query = database.clicks.select
      .p_where(_.url eqs ?)
      .p_and(_.timestamp > ?)
      .p_and(_.timestamp < ?)
      .prepare()

    val operation = for {
      select <- query.bind(url, min, max).fetch()
    } yield select

    operation.successful { results =>
      results.foreach { result =>
        result.url shouldEqual url
        result.timestamp should be > min
        result.timestamp should be < max
      }

      val rangeClicks = clicks.filter(r => r.timestamp > min && r.timestamp < max)

      rangeClicks.foreach { click =>
        results should contain (click)
      }
    }
  }
}
