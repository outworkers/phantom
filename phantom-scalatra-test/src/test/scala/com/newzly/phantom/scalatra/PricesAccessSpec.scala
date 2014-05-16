package com.newzly.phantom.scalatra

import scala.concurrent.blocking
import scala.concurrent.duration._
import scala.util.Random
import dispatch.{Http, url}, dispatch.Defaults._, dispatch.as
import org.joda.time.LocalDate
import org.joda.time.format.DateTimeFormat
import org.json4s.{DefaultFormats, Formats}
import org.scalatest.concurrent.{PatienceConfiguration, AsyncAssertions}
import org.scalatest.{BeforeAndAfterAll, FlatSpec, Matchers}
import com.newzly.phantom.scalatra.server.{ScalatraBootstrap, JettyLauncher}
import com.newzly.phantom.tables.{OptionPrice, EquityPrice}
import com.newzly.util.testing.AsyncAssertionsHelper._


class PricesAccessSpec extends FlatSpec with BeforeAndAfterAll with AsyncAssertions with Matchers {

  private val dateFormat = DateTimeFormat.forPattern("YYYYMMdd")

  implicit val s: PatienceConfiguration.Timeout = timeout(10 seconds)

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

  def optionPrices(id: String, from: LocalDate, to: LocalDate) = {
    val accessUrl = s"http://localhost:${JettyLauncher.port}/prices/option/$id/from/${dateFormat.print(from)}/to/${dateFormat.print(to)}"
    url(accessUrl)
  }

  "Prices Servlet" should "return correct equity prices for Apple" in {
    import ScalatraBootstrap._

    val request = Http(equityPrices(AAPL, new LocalDate(2014, 1, 1), new LocalDate(2014, 1, 10)) OK as.json4s.Json)
    val prices = request.map(json => json.extract[Seq[EquityPrice]])

    prices.successful {
      res => {
        res.size shouldEqual ScalatraBootstrap.ApplePrices.size
        res.map(_.value) shouldEqual ScalatraBootstrap.ApplePrices.map(_.value)
      }
    }
  }
  it should "return correct equity and option prices for Apple / several pararel requests" in {
    import ScalatraBootstrap._

    def expectedEquityForDateRange(start: LocalDate, end: LocalDate): Seq[EquityPrice] =
      ApplePrices.filter { case EquityPrice(_, tradeDate, _, _, _ ) => !tradeDate.isBefore(start) && !tradeDate.isAfter(end)}

    def expectedEquityOptionsForDateRange(start: LocalDate, end: LocalDate): Seq[OptionPrice] =
      AppleOptionPrices.filter { case OptionPrice(_, tradeDate, _, _, _, _) => !tradeDate.isBefore(start) && !tradeDate.isAfter(end)}

    (1 to 30).par.foreach { i =>
      Thread.sleep(Random.nextInt(2500).toLong)
      val startDay = Random.nextInt(8) + 1 // 1 to 8
      val endDay = startDay + Random.nextInt(10 - startDay) + 1
      val from: LocalDate = new LocalDate(2014, 1, startDay)
      val to: LocalDate = new LocalDate(2014, 1, endDay)
      val eqRespFuture = Http(equityPrices(AAPL, from, to) OK as.json4s.Json).map(_.extract[Seq[EquityPrice]])
      val eqOptionRespFuture = Http(optionPrices(AAPLOption, from, to) OK as.json4s.Json).map(_.extract[Seq[OptionPrice]])

      eqRespFuture.successful(_.map(_.value) shouldEqual expectedEquityForDateRange(from, to).map(_.value))
      eqOptionRespFuture.successful(_.map(_.value) shouldEqual expectedEquityOptionsForDateRange(from, to).map(_.value))
    }


  }


}