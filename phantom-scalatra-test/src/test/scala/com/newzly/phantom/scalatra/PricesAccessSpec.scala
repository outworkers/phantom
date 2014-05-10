package com.newzly.phantom.scalatra

import scala.concurrent.blocking
import scala.concurrent.duration._

import org.joda.time.LocalDate
import org.joda.time.format.DateTimeFormat
import org.json4s.{ DefaultFormats, Formats }
import org.scalatest.{ BeforeAndAfterAll, FlatSpec, Matchers, ParallelTestExecution }
import org.scalatest.concurrent.AsyncAssertions

import com.newzly.phantom.scalatra.server.{ScalatraBootstrap, JettyLauncher}
import com.newzly.phantom.tables.EquityPrice
import com.newzly.util.testing.AsyncAssertionsHelper._

import dispatch.{ Http, url }, dispatch.Defaults._, dispatch.as


class PricesAccessSpec extends FlatSpec with BeforeAndAfterAll with AsyncAssertions with Matchers with ParallelTestExecution {

  private val dateFormat = DateTimeFormat.forPattern("YYYYMMdd")

  private implicit val jsonFormats: Formats =
    DefaultFormats.withBigDecimal ++ org.json4s.ext.JodaTimeSerializers.all

  override protected def beforeAll() {
    blocking {
      super.beforeAll()
      JettyLauncher.startEmbeddedJetty()
    }
  }

  def equityPrices(id: String, from: LocalDate, to: LocalDate) = {
    val accessUrl = s"http://localhost:${JettyLauncher.port}/prices/equity/$id/from/${dateFormat.print(from)}/to/${dateFormat.print(to)}"
    url(accessUrl)
  }

  "Prices Servlet" should "return correct equity prices for Apple" in {
    import ScalatraBootstrap._

    val request = Http(equityPrices(AAPL, new LocalDate(2014, 1, 1), new LocalDate(2014, 1, 10)) OK as.json4s.Json)
    val prices = request.map(json => json.extract[Seq[EquityPrice]]), 10.seconds

    prices.successful {
      res => {
        res.size shouldEqual ScalatraBootstrap.ApplePrices.size
        res.map(_.value) shouldEqual ScalatraBootstrap.ApplePrices.map(_.value)
      }
    }
  }
}