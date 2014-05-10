package com.newzly.phantom.scalatra.server

import javax.servlet.ServletContext
import scala.concurrent.Await
import scala.concurrent.duration._

import org.joda.time.{DateTime, LocalDate}
import org.scalatra.LifeCycle

import com.newzly.phantom.Implicits._
import com.newzly.phantom.tables.{ EquityPrice, EquityPrices, OptionPrice, OptionPrices }

object ScalatraBootstrap {
  val now = new DateTime()

  val AAPL = "AAPL"
  val AAPLOption = "AAPL:500"

  val ApplePrices = Seq(
    EquityPrice(AAPL, new LocalDate(2014, 1, 1), "NASDAQ", now, BigDecimal("500.00")),
    EquityPrice(AAPL, new LocalDate(2014, 1, 2), "NASDAQ", now, BigDecimal("501.01")),
    EquityPrice(AAPL, new LocalDate(2014, 1, 3), "NASDAQ", now, BigDecimal("502.02")),
    EquityPrice(AAPL, new LocalDate(2014, 1, 4), "NASDAQ", now, BigDecimal("503.03")),
    EquityPrice(AAPL, new LocalDate(2014, 1, 5), "NASDAQ", now, BigDecimal("504.04")),
    EquityPrice(AAPL, new LocalDate(2014, 1, 6), "NASDAQ", now, BigDecimal("505.05"))
  )

  val AppleOptionPrices = Seq(
    OptionPrice(AAPLOption, new LocalDate(2014, 1, 1), "NASDAQ", now, BigDecimal("500.00"), BigDecimal("1.00")),
    OptionPrice(AAPLOption, new LocalDate(2014, 1, 2), "NASDAQ", now, BigDecimal("500.00"), BigDecimal("2.01")),
    OptionPrice(AAPLOption, new LocalDate(2014, 1, 3), "NASDAQ", now, BigDecimal("500.00"), BigDecimal("3.02")),
    OptionPrice(AAPLOption, new LocalDate(2014, 1, 4), "NASDAQ", now, BigDecimal("500.00"), BigDecimal("4.03")),
    OptionPrice(AAPLOption, new LocalDate(2014, 1, 5), "NASDAQ", now, BigDecimal("500.00"), BigDecimal("5.04")),
    OptionPrice(AAPLOption, new LocalDate(2014, 1, 6), "NASDAQ", now, BigDecimal("500.00"), BigDecimal("6.05"))
  )
}

class ScalatraBootstrap extends LifeCycle with CassandraCluster {
  import ScalatraBootstrap._

  override def init(context: ServletContext) {
    // Create cassandra keyspace in startup
    ensureKeyspaceExists()

    // Create prices tables
    Await.result(EquityPrices.create.future(), 10.seconds)
    Await.result(OptionPrices.create.future(), 10.seconds)

    // Insert prices
    val insertApplePrices = ApplePrices.map(EquityPrices.insertPrice).foldLeft(BatchStatement()) {
      (batch, insertQuery) => batch.add(insertQuery)
    }
    Await.result(insertApplePrices.future(), 10.seconds)

    val insertAppleOptionPrices = AppleOptionPrices.map(OptionPrices.insertPrice).foldLeft(BatchStatement()) {
      (batch, insertQuery) => batch.add(insertQuery)
    }

    Await.result(insertAppleOptionPrices.future(), 10.seconds)

    // Mount prices servlet
    context mount (new PricesAccess, "/*")
  }
}