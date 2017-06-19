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

import com.datastax.driver.core.Session
import com.outworkers.phantom.builder.QueryBuilder
import com.outworkers.phantom.builder.query.engine.CQLQuery
import com.outworkers.phantom.builder.syntax.CQLSyntax
import com.outworkers.phantom.connectors.SessionAugmenterImplicits

sealed abstract class CacheProperty(val qb: CQLQuery) extends TablePropertyClause

private[phantom] trait CachingStrategies {

  private[this] def caching(strategy: String) = {
    CQLQuery(CQLSyntax.Symbols.`{`).forcePad
      .appendSingleQuote(CQLSyntax.CacheStrategies.Caching)
      .forcePad.append(CQLSyntax.Symbols.colon)
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

    def rows_per_partition(value: Int): QType = {
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
