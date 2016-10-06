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

import com.datastax.driver.core.Session
import com.websudos.phantom.builder.QueryBuilder
import com.websudos.phantom.builder.query.CQLQuery
import com.websudos.phantom.builder.syntax.CQLSyntax
import com.websudos.phantom.connectors.SessionAugmenterImplicits


sealed abstract class CacheProperty(val qb: CQLQuery) extends TablePropertyClause

private[phantom] trait CachingStrategies {

  private[this] def caching(strategy: String) = {
    CQLQuery(CQLSyntax.Symbols.`{`).forcePad
      .appendSingleQuote(CQLSyntax.CacheStrategies.Caching)
      .forcePad.append(CQLSyntax.Symbols.`:`)
      .forcePad.appendSingleQuote(strategy)
  }

  abstract class SpecificCacheProperty[
    QType <: SpecificCacheProperty[QType]
  ](override val qb: CQLQuery) extends CacheProperty(qb) {
    def instance(qb: CQLQuery): QType

    def keys(value: String = CQLSyntax.CacheStrategies.All): QType = {
      instance(QueryBuilder.Create.Caching.keys(qb, value))
    }

    def rows(value: String = CQLSyntax.CacheStrategies.All): QType = {
      instance(QueryBuilder.Create.Caching.rowsPerPartition(qb, value))
    }

    def rows_per_partition(value: String = CQLSyntax.CacheStrategies.All): QType = {
      instance(QueryBuilder.Create.Caching.rowsPerPartition(qb, value))
    }
  }

  /**
    * A class wrapping a "none" cache property definition.
    * @param qb The query builder string that wraps the property definition.
    * @param wrapped If the propery is escaped it will be wrapped in curly braces at the end.
    *
    * Example: {{{
    *   // if the escaped parameter is set to true
    *   cache: {'rows_per_partition': 'none'}
    *
    *   // if set to false
    *
    * }}}
    * @param session
    */
  sealed class NoneCache(
    override val qb: CQLQuery,
    override val wrapped: Boolean
  )(implicit session: Session) extends SpecificCacheProperty[NoneCache](qb) {
    override def instance(qb: CQLQuery): NoneCache = new NoneCache(qb, wrapped)
  }

  sealed class KeysOnly(
    override val qb: CQLQuery,
    override val wrapped: Boolean
  )(implicit session: Session) extends SpecificCacheProperty[KeysOnly](qb) {
    override def instance(qb: CQLQuery): KeysOnly = new KeysOnly(qb, wrapped)
  }

  sealed class RowsOnly(
    override val qb: CQLQuery,
    override val wrapped: Boolean
  )(implicit session: Session) extends SpecificCacheProperty[RowsOnly](qb) {
    override def instance(qb: CQLQuery): RowsOnly = new RowsOnly(qb, wrapped)
  }

  sealed class AllCache(
    override val qb: CQLQuery,
    override val wrapped: Boolean
  )(implicit session: Session) extends SpecificCacheProperty[AllCache](qb) {
    override def instance(qb: CQLQuery): AllCache = new AllCache(qb, wrapped)
  }

  object None extends SessionAugmenterImplicits {
    def apply()(implicit session: Session): NoneCache = {
      if (session.v4orNewer) {
        new NoneCache(CQLQuery.empty, wrapped = true)
          .keys(CQLSyntax.CacheStrategies.None)
          .rows(CQLSyntax.CacheStrategies.None)
      } else {
        new NoneCache(CQLQuery(CQLSyntax.CacheStrategies.None), false)
      }
    }
  }

  object KeysOnly extends SessionAugmenterImplicits {
    def apply()(implicit session: Session): KeysOnly = {
      if (session.v4orNewer) {
        new KeysOnly(CQLQuery.empty, wrapped = true).keys().rows_per_partition(CQLSyntax.CacheStrategies.None)
      } else {
        new KeysOnly(CQLQuery(CQLSyntax.CacheStrategies.KeysOnly), false)
      }
    }
  }

  object RowsOnly extends SessionAugmenterImplicits {
    def apply()(implicit session: Session): RowsOnly = {
      if (session.v4orNewer) {
        new RowsOnly(CQLQuery.empty, true).rows()
      } else {
        new RowsOnly(CQLQuery(CQLSyntax.CacheStrategies.RowsOnly), false)
      }
    }
  }

  object All extends SessionAugmenterImplicits {
    def apply()(implicit session: Session): AllCache = {
      if (session.v4orNewer) {
        new AllCache(CQLQuery.empty, true).keys().rows()
      } else {
        new AllCache(CQLQuery(CQLSyntax.CacheStrategies.All), false)
      }
    }
  }
}

object Caching extends CachingStrategies

class CachingBuilder extends TableProperty {

  def eqs(strategy: CacheProperty): TablePropertyClause = {
    new TablePropertyClause {
      override def qb: CQLQuery = {
        QueryBuilder.Create.caching(strategy.qb.queryString, strategy.wrapped)
      }
    }
  }

}
