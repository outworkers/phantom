package com.websudos.phantom.server

import java.util.Date
import org.joda.time.{DateTime, LocalDate}

import com.datastax.driver.core.Row
import com.websudos.phantom.Implicits._
import com.websudos.phantom.query.InsertQuery
import com.websudos.phantom.testing.PhantomCassandraConnector


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

object EquityPrices extends EquityPrices with PhantomCassandraConnector {
  override val tableName: String = "EquityPrices"


  def insertPrice(price: EquityPrice) =
    insert.
      value(_.instrumentId, price.instrumentId).
      value(_.tradeDate, price.tradeDate.toDate).
      value(_.exchangeCode, price.exchangeCode).
      value(_.t, price.t).
      value(_.value, price.value)

}

object OptionPrices extends OptionPrices with PhantomCassandraConnector {
  override val tableName: String = "OptionPrices"

  def insertPrice(price: OptionPrice): InsertQuery[OptionPrices, OptionPrice] = {
    insert
      .value(_.instrumentId, price.instrumentId)
      .value(_.tradeDate, price.tradeDate.toDate)
      .value(_.exchangeCode, price.exchangeCode)
      .value(_.t, price.t)
      .value(_.strikePrice, price.strikePrice)
      .value(_.value, price.value)
  }

}
