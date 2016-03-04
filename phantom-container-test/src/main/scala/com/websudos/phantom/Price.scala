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

import java.util.Date

import com.datastax.driver.core.Row
import com.websudos.phantom.builder.query.InsertQuery
import com.websudos.phantom.db.DatabaseImpl
import com.websudos.phantom.dsl._
import org.joda.time.{DateTime, LocalDate}

sealed trait Price {
  def instrumentId: String
  def tradeDate: LocalDate
  def exchangeCode: String
  def t: DateTime
}

case class EquityPrice(
  instrumentId: String,
  tradeDate: LocalDate,
  exchangeCode: String,
  t: DateTime,
  value: BigDecimal
)

case class OptionPrice(
  instrumentId: String,
  tradeDate: LocalDate,
  exchangeCode: String,
  t: DateTime,
  strikePrice: BigDecimal,
  value: BigDecimal
)

sealed class EquityPrices extends CassandraTable[EquityPrices, EquityPrice] {
  object instrumentId extends StringColumn(this) with PartitionKey[String]

  object tradeDate extends DateColumn(this) with PrimaryKey[Date]

  object exchangeCode extends StringColumn(this) with PrimaryKey[String]

  object t extends DateTimeColumn(this) with PrimaryKey[DateTime]

  object value extends BigDecimalColumn(this)

  override def fromRow(r: Row): EquityPrice =
    EquityPrice(instrumentId(r), new LocalDate(tradeDate(r)), exchangeCode(r), t(r), value(r))
}

sealed class OptionPrices extends CassandraTable[OptionPrices, OptionPrice] {

  object instrumentId extends StringColumn(this) with PartitionKey[String]

  object tradeDate extends DateColumn(this) with PrimaryKey[Date]

  object exchangeCode extends StringColumn(this) with PrimaryKey[String]

  object t extends DateTimeColumn(this) with PrimaryKey[DateTime]

  object strikePrice extends BigDecimalColumn(this) with PrimaryKey[BigDecimal]

  object value extends BigDecimalColumn(this)

  override def fromRow(r: Row): OptionPrice =
    OptionPrice(instrumentId(r), new LocalDate(tradeDate(r)), exchangeCode(r), t(r), strikePrice(r), value(r))
}

abstract class ConcreteEquityPrices extends EquityPrices with RootConnector {
  override val tableName: String = "EquityPrices"


  def insertPrice(price: EquityPrice): InsertQuery.Default[EquityPrices, EquityPrice] =
    insert.
      value(_.instrumentId, price.instrumentId).
      value(_.tradeDate, price.tradeDate.toDate).
      value(_.exchangeCode, price.exchangeCode).
      value(_.t, price.t).
      value(_.value, price.value)

}

abstract class ConcreteOptionPrices extends OptionPrices with RootConnector {
  override val tableName: String = "OptionPrices"

  def insertPrice(price: OptionPrice): InsertQuery.Default[OptionPrices, OptionPrice] = {
    insert
      .value(_.instrumentId, price.instrumentId)
      .value(_.tradeDate, price.tradeDate.toDate)
      .value(_.exchangeCode, price.exchangeCode)
      .value(_.t, price.t)
      .value(_.strikePrice, price.strikePrice)
      .value(_.value, price.value)
  }
}

class EquityDatabase(override val connector: KeySpaceDef) extends DatabaseImpl(connector) {
  object equityPrices extends ConcreteEquityPrices with connector.Connector
  object optionPrices extends ConcreteOptionPrices with connector.Connector
}

object EquityConnector {
  val default = ContactPoint.local.noHeartbeat().keySpace("equities")
}

object EquityDatabase extends EquityDatabase(EquityConnector.default)

