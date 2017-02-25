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
import org.joda.time.Seconds

import scala.concurrent.duration.FiniteDuration

/**
  * A root implementation trait of a CQL table option.
  * These are implemented with respect to the CQL 3.0 reference available here:
  * {{ http://www.datastax.com/documentation/cql/3.0/cql/cql_reference/tabProp.html }}
  */
trait TableProperty

trait TablePropertyClause {
  def qb: CQLQuery

  def wrapped: Boolean = false
}

trait ClauseBuilder[T] extends TablePropertyClause {

  def options: OptionPart

  override def qb: CQLQuery = options build CQLQuery.empty

  protected[this] def instance(opts: OptionPart): T

  protected[this] def instance(qb: CQLQuery): T = {
    instance(options append qb)
  }

  def option(key: String, value: String): T = {
    val qb = QueryBuilder.Utils.option(
      CQLQuery.escape(key),
      CQLSyntax.Symbols.colon,
      value
    )

    instance(qb)
  }
}

sealed trait WithBound
sealed trait WithChainned extends WithBound
sealed trait WithUnchainned extends WithBound

sealed trait CompactionBound
sealed trait SpecifiedCompaction extends CompactionBound
sealed trait UnspecifiedCompaction extends CompactionBound

private[phantom] class TimeToLiveBuilder extends TableProperty {

  def eqs(time: Long): TablePropertyClause = {
    new TablePropertyClause {
      def qb: CQLQuery = QueryBuilder.Create.default_time_to_live(time.toString)
    }
  }

  def eqs(duration: Seconds): TablePropertyClause = eqs(duration.getSeconds.toLong)

  def eqs(duration: FiniteDuration): TablePropertyClause = eqs(duration.toSeconds)
}

private[phantom] class GcGraceSecondsBuilder extends TableProperty {
  def eqs(clause: Seconds): TablePropertyClause = {
    new TablePropertyClause {
      def qb: CQLQuery = QueryBuilder.Create.gc_grace_seconds(clause.getSeconds.toString)
    }
  }

  def eqs(duration: FiniteDuration): TablePropertyClause = {
    new TablePropertyClause {
      def qb: CQLQuery = QueryBuilder.Create.gc_grace_seconds(duration.toSeconds.toString)
    }
  }
}

private[phantom] class ReadRepairChanceBuilder extends TableProperty {
  def eqs(clause: Double): TablePropertyClause = {
    new TablePropertyClause {
      def qb: CQLQuery = QueryBuilder.Create.read_repair_chance(clause.toString)
    }
  }
}

private[phantom] class ReplicateOnWriteBuilder extends TableProperty {
  def apply(clause: Boolean): TablePropertyClause = {
    new TablePropertyClause {
      def qb: CQLQuery = QueryBuilder.Create.replicate_on_write(clause.toString)
    }
  }

  def eqs(clause: Boolean): TablePropertyClause = apply(clause)
}

private[phantom] class BloomFilterFpChanceBuilder extends TableProperty {
  def eqs(clause: Double): TablePropertyClause = {
    new TablePropertyClause {
      def qb: CQLQuery = QueryBuilder.Create.bloom_filter_fp_chance(clause.toString)
    }
  }
}

private[phantom] class DcLocalReadRepairChanceBuilder extends TableProperty {
  def eqs(clause: Double): TablePropertyClause = {
    new TablePropertyClause {
      def qb: CQLQuery = QueryBuilder.Create.dclocal_read_repair_chance(clause.toString)
    }
  }
}

private[phantom] class CommentClauseBuilder extends TableProperty {
  def eqs(clause: String): TablePropertyClause = {
    new TablePropertyClause {
      def qb: CQLQuery = QueryBuilder.Create.comment(clause)
    }
  }
}