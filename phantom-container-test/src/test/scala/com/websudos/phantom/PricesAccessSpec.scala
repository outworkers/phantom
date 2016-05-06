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
 * - Explicit consent must be obtained from the copyright owner, Outworkers Limited before any redistribution is made.
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
package com.websudos.phantom

import net.liftweb.json.{DefaultFormats, Formats}

import scala.concurrent.blocking
import scala.concurrent.duration._
import scala.util.Random

import org.joda.time.LocalDate
import org.joda.time.format.DateTimeFormat
import org.json4s.{DefaultFormats, Formats}
import org.scalatest.concurrent.PatienceConfiguration

import com.websudos.util.testing._
import com.websudos.phantom.server.ScalatraBootstrap.{AAPL, AAPLOption, AppleOptionPrices, ApplePrices}
import com.websudos.phantom.server._
import dispatch.{Http, as, url}

class PricesAccessSpec extends PhantomSuite {

  private[this] val dateFormat = DateTimeFormat.forPattern("YYYYMMdd")

  val port = 8081

  implicit val s: PatienceConfiguration.Timeout = timeout(10 seconds)
  implicit val executor = scala.concurrent.ExecutionContext.Implicits.global

  private implicit val jsonFormats: Formats =
    DefaultFormats.withBigDecimal ++ org.json4s.ext.JodaTimeSerializers.all

  override def beforeAll(): Unit = {
    super.beforeAll()
  }

  def equityPrices(id: String, from: LocalDate, to: LocalDate) = {
    val accessUrl = s"http://localhost:$port/prices/equity/$id/from/${dateFormat.print(from)}/to/${dateFormat.print(to)}"
    url(accessUrl)
  }

  def optionPrices(id: String, from: LocalDate, to: LocalDate) = {
    val accessUrl = s"http://localhost:$port/prices/option/$id/from/${dateFormat.print(from)}/to/${dateFormat.print(to)}"
    url(accessUrl)
  }

  "Prices Servlet" should "return correct equity prices for Apple stock" in {

    val chain = for {
      req <- Http(equityPrices(
        AAPL,
        new LocalDate(2014, 1, 1),
        new LocalDate(2014, 1, 10)
      ) OK as.json4s.Json)
      .map(json => json.extract[Seq[EquityPrice]])
    } yield req

    chain.successful {
      res => {
        res.size shouldEqual ScalatraBootstrap.ApplePrices.size
        res.map(_.value) shouldEqual ScalatraBootstrap.ApplePrices.map(_.value)
      }
    }
  }

  it should "return correct equity and option prices for Apple stock after several parallel requests" in {

    def expectedEquityForDateRange(start: LocalDate, end: LocalDate): Seq[EquityPrice] = {
      ApplePrices.filter(price => !price.tradeDate.isBefore(start) && !price.tradeDate.isAfter(end))
    }

    def expectedEquityOptionsForDateRange(start: LocalDate, end: LocalDate): Seq[OptionPrice] = {
      AppleOptionPrices.filter(price => !price.tradeDate.isBefore(start) && !price.tradeDate.isAfter(end))
    }

    (1 to 30).par.foreach { i =>
      Thread.sleep(Random.nextInt(2500).toLong)
      val startDay = Random.nextInt(8) + 1 // 1 to 8
      val endDay = startDay + Random.nextInt(10 - startDay) + 1
      val from: LocalDate = new LocalDate(2014, 1, startDay)
      val to: LocalDate = new LocalDate(2014, 1, endDay)
      val eqRespFuture = Http(equityPrices(AAPL, from, to) OK as.json4s.Json).map(_.extract[Seq[EquityPrice]])
      val eqOptionRespFuture = Http(optionPrices(AAPLOption, from, to) OK as.json4s.Json).map(_.extract[Seq[OptionPrice]])

      val chain = for {
        resp1 <- eqRespFuture
        resp2 <- eqOptionRespFuture
      } yield (resp1, resp2)

      chain.successful {
        case (res1, res2) => {
          res1.map(_.value) shouldEqual expectedEquityForDateRange(from, to).map(_.value)
          res2.map(_.value) shouldEqual expectedEquityOptionsForDateRange(from, to).map(_.value)
        }
      }
    }
  }
}
