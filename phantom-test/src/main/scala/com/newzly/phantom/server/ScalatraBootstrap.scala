/*
 * Copyright 2013 newzly ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.newzly.phantom.server

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