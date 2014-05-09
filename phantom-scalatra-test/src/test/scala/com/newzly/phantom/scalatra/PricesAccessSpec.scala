package com.newzly.phantom.scalatra

import com.newzly.phantom.scalatra.server.{ScalatraBootstrap, JettyLauncher}
import com.newzly.phantom.tables.EquityPrice
import dispatch.{ Http, url }, dispatch.Defaults._, dispatch.as
import org.joda.time.LocalDate
import org.joda.time.format.DateTimeFormat
import org.json4s._
import org.scalatest.{BeforeAndAfterAll, FlatSpec}
import scala.concurrent.Await
import scala.concurrent.duration._

class PricesAccessSpec extends FlatSpec with BeforeAndAfterAll {

  private val dateFormat = DateTimeFormat.forPattern("YYYYMMdd")

  private implicit val jsonFormats: Formats =
    DefaultFormats.withBigDecimal ++ org.json4s.ext.JodaTimeSerializers.all


  override protected def beforeAll() {
    super.beforeAll()

    JettyLauncher.startEmbeddedJetty()
  }

  def equityPrices(id: String, from: LocalDate, to: LocalDate) = {
    val accessUrl = s"http://localhost:${JettyLauncher.port}/prices/equity/$id/from/${dateFormat.print(from)}/to/${dateFormat.print(to)}"
    url(accessUrl)
  }

  "Prices Servlet" should "return correct equity prices for Apple" in {
    import ScalatraBootstrap._

    val request = Http(equityPrices(AAPL, new LocalDate(2014, 1, 1), new LocalDate(2014, 1, 10)) OK as.json4s.Json)
    val prices = Await.result(request.map(json => json.extract[Seq[EquityPrice]]), 10.seconds)

    assert(prices.size == ScalatraBootstrap.ApplePrices.size)
    assert(prices.map(_.value) == ScalatraBootstrap.ApplePrices.map(_.value))
  }
}