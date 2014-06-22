package com.newzly.phantom.server

import java.util.Date

import com.datastax.driver.core.Row
import com.newzly.util.testing.Sampler
import com.websudos.phantom.Implicits._
import com.websudos.phantom.helper.{ModelSampler, TestSampler}
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

object EquityPrices extends EquityPrices with TestSampler[EquityPrices, EquityPrice] with ModelSampler[EquityPrice] {
  override val tableName: String = "EquityPrices"

  override def sample: EquityPrice = EquityPrice(
    Sampler.getARandomString,
    new LocalDate(),
    Sampler.getARandomString,
    new DateTime(),
    BigDecimal(Sampler.getARandomInteger())
  )

  def insertPrice(price: EquityPrice) =
    insert.
      value(_.instrumentId, price.instrumentId).
      value(_.tradeDate, price.tradeDate.toDate).
      value(_.exchangeCode, price.exchangeCode).
      value(_.t, price.t).
      value(_.value, price.value)

}

object OptionPrices extends OptionPrices with TestSampler[OptionPrices, OptionPrice] with ModelSampler[OptionPrice] {
  override val tableName: String = "OptionPrices"

  override def sample: OptionPrice = OptionPrice(
    Sampler.getARandomString,
    new LocalDate(),
    Sampler.getARandomString,
    new DateTime(),
    BigDecimal(Sampler.getARandomInteger()),
    BigDecimal(Sampler.getARandomInteger())
  )

  def insertPrice(price: OptionPrice) =
    insert.
      value(_.instrumentId, price.instrumentId).
      value(_.tradeDate, price.tradeDate.toDate).
      value(_.exchangeCode, price.exchangeCode).
      value(_.t, price.t).
      value(_.strikePrice, price.strikePrice).
      value(_.value, price.value)
}
