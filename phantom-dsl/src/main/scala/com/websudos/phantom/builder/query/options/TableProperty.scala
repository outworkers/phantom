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
package com.websudos.phantom.builder.query.options

import com.websudos.phantom.builder.QueryBuilder
import com.websudos.phantom.builder.query.{CQLQuery, OptionPart}
import com.websudos.phantom.builder.syntax.CQLSyntax
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

  protected[this] def instance(qb: CQLQuery): T = instance(options append qb)

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