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
package com.websudos.phantom.server

import scala.concurrent.Await
import scala.concurrent.duration._

import org.joda.time.format.DateTimeFormat
import org.json4s.{DefaultFormats, Formats}
import org.scalatra.ScalatraServlet
import org.scalatra.json.JacksonJsonSupport
import org.scalatra.scalate.ScalateSupport

import com.websudos.phantom.Implicits._
import com.websudos.phantom.testing.PhantomCassandraConnector


class PricesAccess extends ScalatraServlet with JacksonJsonSupport with ScalateSupport with PhantomCassandraConnector {

  private[this] val dateFormat = DateTimeFormat.forPattern("YYYYMMdd")

  protected implicit val jsonFormats: Formats =
    DefaultFormats.withBigDecimal ++ org.json4s.ext.JodaTimeSerializers.all

  before() {
    contentType = formats("json")
  }

  get("/prices/equity/:id/from/:from/to/:to") {
    val id = params("id")
    val from = dateFormat.parseLocalDate(params("from"))
    val to = dateFormat.parseLocalDate(params("to"))

    val prices = EquityPrices.select
      .where(_.instrumentId eqs id)
      .and(_.tradeDate gte from.toDate)
      .and(_.tradeDate lte to.toDate)
      .fetch()

    Await.result(prices, 10.seconds)
  }

  get("/prices/option/:id/from/:from/to/:to") {
    val id = params("id")
    val from = dateFormat.parseLocalDate(params("from"))
    val to = dateFormat.parseLocalDate(params("to"))

    val prices = OptionPrices.select
      .where(_.instrumentId eqs id)
      .and(_.tradeDate gte from.toDate)
      .and(_.tradeDate lte to.toDate)
      .fetch()

    Await.result(prices, 10.seconds)
  }

}


