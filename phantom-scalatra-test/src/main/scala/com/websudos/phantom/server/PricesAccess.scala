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


