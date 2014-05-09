package com.newzly.phantom.tables

import com.datastax.driver.core.Row

import com.newzly.phantom.CassandraTable
import com.newzly.phantom.Implicits.{BigDecimalColumn, StringColumn}
import com.newzly.phantom.column.{DateTimeColumn, DateColumn}
import com.newzly.phantom.helper.{ModelSampler, TestSampler}
import com.newzly.phantom.keys.{PrimaryKey, PartitionKey}
import com.newzly.util.testing.Sampler

import java.util.Date
import org.joda.time.{DateTime, LocalDate}

sealed trait Price {
  def instrumentId: String
  def tradeDate: LocalDate
  def exchangeCode: String
  def t: DateTime
}

case class EquityPrice(instrumentId: String,
                       tradeDate: LocalDate,
                       exchangeCode: String,
                       t: DateTime,
                       value: BigDecimal)

case class OptionPrice(instrumentId: String,
                       tradeDate: LocalDate,
                       exchangeCode: String,
                       t: DateTime,
                       strikePrice: BigDecimal,
                       value: BigDecimal)

sealed trait Prices[T <: CassandraTable[T, R], R] extends CassandraTable[T, R] {

  object instrumentId extends StringColumn(this) with PartitionKey[String]

  object tradeDate extends DateColumn(this) with PrimaryKey[Date]

  object exchangeCode extends StringColumn(this) with PrimaryKey[String]

  object t extends DateTimeColumn(this) with PrimaryKey[DateTime]
}

sealed class EquityPrices extends Prices[EquityPrices, EquityPrice] {
  object value extends BigDecimalColumn(this)

  override def fromRow(r: Row): EquityPrice =
    EquityPrice(instrumentId(r), new LocalDate(tradeDate(r)), exchangeCode(r), t(r), value(r))
}

sealed class OptionPrices extends Prices[OptionPrices, OptionPrice] {

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
}