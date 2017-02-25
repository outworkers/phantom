/*
 * Copyright 2013 - 2017 Outworkers Ltd.
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
package com.outworkers.phantom.builder.query.options

import com.outworkers.phantom.builder.QueryBuilder
import com.outworkers.phantom.builder.query.engine.CQLQuery
import com.outworkers.phantom.builder.query.OptionPart
import com.outworkers.phantom.builder.syntax.CQLSyntax

import scala.concurrent.duration.TimeUnit

sealed abstract class CompactionProperties[
  T <: CompactionProperties[T]
](val options: OptionPart) extends ClauseBuilder[T] {

  def enabled(flag: Boolean): T = option(CQLSyntax.CompactionOptions.enabled, flag.toString)

  def tombstone_compaction_interval(interval: Long): T = {
    option(
      CQLSyntax.CompactionOptions.tombstone_compaction_interval,
      interval.toString
    )
  }

  def tombstone_threshold(value: Double): T = {
    option(
      CQLSyntax.CompactionOptions.tombstone_threshold,
      value.toString
    )
  }

  def unchecked_tombstone_compaction(value: Double): T = {
    option(
      CQLSyntax.CompactionOptions.unchecked_tombstone_compaction,
      value.toString
    )
  }
}

private[phantom] trait CompactionStrategies {

  private[this] def strategy(strategy: String): OptionPart = {
    OptionPart(
      CQLQuery.empty
      .appendSingleQuote(CQLSyntax.CompactionOptions.clz)
      .append(CQLSyntax.Symbols.colon)
      .forcePad.appendSingleQuote(strategy)
    )
  }

  sealed class SizeTieredCompactionStrategy(options: OptionPart)
    extends CompactionProperties[SizeTieredCompactionStrategy](options) {

    def min_sstable_size(value: Int): SizeTieredCompactionStrategy = {
      option(
        CQLSyntax.CompactionOptions.min_sstable_size,
        value.toString
      )
    }

    def max_threshold(value: Int): SizeTieredCompactionStrategy = {
      option(
        CQLSyntax.CompactionOptions.max_threshold,
        value.toString
      )
    }

    def min_threshold(value: Int): SizeTieredCompactionStrategy = {
      option(
        CQLSyntax.CompactionOptions.min_threshold,
        value.toString
      )
    }

    def bucket_high(value: Double): SizeTieredCompactionStrategy = {
      option(
        CQLSyntax.CompactionOptions.bucket_high,
        value.toString
      )
    }

    def cold_reads_to_omit(value: Double): SizeTieredCompactionStrategy = {
      option(
        CQLSyntax.CompactionOptions.cold_reads_to_omit,
        value.toString
      )
    }

    def bucket_low(value: Double): SizeTieredCompactionStrategy = {
      option(
        CQLSyntax.CompactionOptions.bucket_low,
        value.toString
      )
    }

    override protected[this] def instance(options: OptionPart): SizeTieredCompactionStrategy = {
      new SizeTieredCompactionStrategy(options)
    }
  }

  sealed class LeveledCompactionStrategy(options: OptionPart)
    extends CompactionProperties[LeveledCompactionStrategy](options) {

    def sstable_size_in_mb(value: Int): LeveledCompactionStrategy = {
      option(
        CQLSyntax.CompactionOptions.sstable_size_in_mb,
        value.toString
      )
    }

    override protected[this] def instance(options: OptionPart): LeveledCompactionStrategy = {
      new LeveledCompactionStrategy(options)
    }
  }

  sealed class TimeWindowCompactionStrategy(options: OptionPart)
    extends CompactionProperties[TimeWindowCompactionStrategy](options) {
    override protected[this] def instance(opts: OptionPart): TimeWindowCompactionStrategy = {
      new TimeWindowCompactionStrategy(opts)
    }

    /**
      * Declares the time unit to write with.
      * This will default to microseconds as per [[http://cassandra.apache.org/doc/latest/operating/compaction.html?highlight=time%20window%20compaction#time-window-compactionstrategy/ the official Cassandra Docs]]
      *
      * The only two valid options are [[java.util.concurrent.TimeUnit.MILLISECONDS]] and
      * [[java.util.concurrent.TimeUnit.MILLISECONDS]].
      *
      * @param unit The [[java.util.concurrent.TimeUnit]] to use, defaults to [[java.util.concurrent.TimeUnit.MICROSECONDS]].
      * @return A compaction strategy builder with a time unit specified.
      */
    def timestamp_resolution(unit: TimeUnit): TimeWindowCompactionStrategy = {
      option(
        CQLSyntax.CompactionOptions.timestamp_resolution,
        CQLQuery.escape(unit.name())
      )
    }

    def compaction_window_size(value: Long): TimeWindowCompactionStrategy = {
      option(
        CQLSyntax.CompactionOptions.compaction_window_size,
        value.toString
      )
    }

    /**
      * Declares the time unit to use with this compaction strategy.
      * This will default to days as per [[http://cassandra.apache.org/doc/latest/operating/compaction.html?highlight=time%20window%20compaction#time-window-compactionstrategy/ the official Cassandra Docs]]
      * @param unit The [[java.util.concurrent.TimeUnit]] to use, defaults to [[java.util.concurrent.TimeUnit.DAYS]].
      * @return A compaction strategy builder with a time unit specified.
      */
    def compaction_window_unit(unit: TimeUnit): TimeWindowCompactionStrategy = {
      option(
        CQLSyntax.CompactionOptions.compaction_window_unit,
        CQLQuery.escape(unit.name())
      )
    }
  }

  sealed class DateTieredCompactionStrategy(options: OptionPart)
    extends CompactionProperties[DateTieredCompactionStrategy](options) {

    override protected[this] def instance(opts: OptionPart): DateTieredCompactionStrategy = {
      new DateTieredCompactionStrategy(opts)
    }

    /**
      * Declares the time unit to write with.
      * This will default to microseconds as per [[http://cassandra.apache.org/doc/latest/operating/compaction.html?highlight=time%20window%20compaction#time-window-compactionstrategy/ the official Cassandra Docs]]
      *
      * The only two valid options are [[java.util.concurrent.TimeUnit.MILLISECONDS]] and
      * [[java.util.concurrent.TimeUnit.MILLISECONDS]].
      *
      * @param unit The [[java.util.concurrent.TimeUnit]] to use, defaults to [[java.util.concurrent.TimeUnit.MICROSECONDS]].
      * @return A compaction strategy builder with a time unit specified.
      */
    def timestamp_resolution(unit: TimeUnit): DateTieredCompactionStrategy = {
      option(
        CQLSyntax.CompactionOptions.timestamp_resolution,
        CQLQuery.escape(unit.name())
      )
    }

    def base_time_seconds(value: Long): DateTieredCompactionStrategy = {
      option(
        CQLSyntax.CompactionOptions.base_time_seconds,
        value.toString
      )
    }

    def max_sstable_age_days(value: Long): DateTieredCompactionStrategy = {
      option(
        CQLSyntax.CompactionOptions.max_sstable_age_days,
        value.toString
      )
    }

    def max_threshold(value: Int): DateTieredCompactionStrategy = {
      option(
        CQLSyntax.CompactionOptions.max_threshold,
        value.toString
      )
    }

    def min_threshold(value: Int): DateTieredCompactionStrategy = {
      option(
        CQLSyntax.CompactionOptions.min_threshold,
        value.toString
      )
    }
  }

  case object SizeTieredCompactionStrategy extends SizeTieredCompactionStrategy(
    strategy(CQLSyntax.CompactionStrategies.sizeTiered)
  )

  case object LeveledCompactionStrategy extends LeveledCompactionStrategy(
    strategy(CQLSyntax.CompactionStrategies.leveled)
  )

  case object DateTieredCompactionStrategy extends DateTieredCompactionStrategy(
    strategy(CQLSyntax.CompactionStrategies.dateTiered)
  )

  case object TimeWindowCompactionStrategy extends TimeWindowCompactionStrategy(
    strategy(CQLSyntax.CompactionStrategies.timeWindow)
  )
}

private[phantom] class CompactionBuilder extends TableProperty {
  def eqs(clause: CompactionProperties[_]): TablePropertyClause = {
    new TablePropertyClause {
      def qb: CQLQuery = QueryBuilder.Create.compaction(clause.qb)
    }
  }
}
