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

import javax.servlet.ServletContext

import com.websudos.phantom.dsl._
import com.websudos.phantom.testkit.PhantomCassandraConnector
import org.joda.time.{DateTime, LocalDate}
import org.scalatra.LifeCycle

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

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

class ScalatraBootstrap extends LifeCycle with PhantomCassandraConnector {

  override def init(context: ServletContext) {

    // Create cassandra keyspace in startup
    // Create prices tables
    Await.ready(EquityPrices.create.ifNotExists().future(), 5.seconds)
    Await.ready(OptionPrices.create.ifNotExists().future(), 5.seconds)

    // Insert prices
    val insertApplePrices = ScalatraBootstrap.ApplePrices.map(EquityPrices.insertPrice).foldLeft(Batch.unlogged) {
      (batch, insertQuery) => batch.add(insertQuery)
    }

    val chain = for {
      truncate <- EquityPrices.truncate.future()
      batch <- insertApplePrices.future()
    } yield batch

    Await.ready(chain, 3.seconds)

    val insertAppleOptionPrices = ScalatraBootstrap.AppleOptionPrices.map(OptionPrices.insertPrice).foldLeft(Batch.unlogged) {
      (batch, insertQuery) => batch.add(insertQuery)
    }

    val chain2 = for {
      truncate <- OptionPrices.truncate.future()
      batch <- insertAppleOptionPrices.future()
    } yield batch

    Await.ready(chain2, 3.seconds)

    // Mount prices servlet
    context mount (new PricesAccess, "/*")
  }
}
