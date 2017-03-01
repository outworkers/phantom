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
package com.outworkers.phantom.builder.query

import com.outworkers.phantom.builder.query.engine.CQLQuery
import com.outworkers.phantom.builder.query.options._
import com.outworkers.phantom.builder.syntax.CQLSyntax

/**
  * A collection of available table property clauses with all the default objects available.
  * This serves as a helper trait for [[com.outworkers.phantom.dsl]] and brings all the relevant options into scope.
  */
private[phantom] trait TablePropertyClauses extends CompactionStrategies with CompressionStrategies {
  object Storage {
    case object CompactStorage extends TablePropertyClause {
      def qb: CQLQuery = CQLQuery(CQLSyntax.StorageMechanisms.CompactStorage)
    }
  }

  /**
  * Helper object used to specify the compression strategy for a table.
  * According to the Cassandra specification, the available strategies are:
  *
  * <ul>
  *   <li>SnappyCompressor</li>
  *   <li>LZ4Compressor</li>
  *   <li>DeflateCompressor</li>
  * </ul>
  *
  * A simple way to define a strategy is by using the {{eqs}} method.
  *
  * {{{
  *  import com.outworkers.phantom.dsl._
  *
  *  SomeTable.create.with(compression eqs SnappyCompressor)
  *
  * }}}
  *
  * Using a compression strategy also allows using the API methods of controlling compressor behaviour:
  *
  * {{{
  *   import com.outworkers.phantom.dsl._
  *   import com.twitter.conversions.storage._
  *
  *   SomeTable.create.`with`(compression eqs SnappyCompressor.chunk_length_kb(50.kilobytes))
  *
  * }}}
  */
  final val compression = new CompressionBuilder

  /**
  * Table creation clause allowing specification of CQL compaction strategies.
  *
  * <ul>
  *   <li>SizeTieredCompactionStrategy</li>
  *   <li>LeveledCompactionStrategy</li>
  *   <li>DateTieredCompactionStrategy</li>
  * </ul>
  *
  * {{{
  *   import com.outworkers.phantom.dsl._
  *
  *   SomeTable.create.`with`(compaction eqs SnappyCompressor)
  * }}}
  */
  final val compaction = new CompactionBuilder

  final val caching = new CachingBuilder

  object Caching extends CachingStrategies

  final val default_time_to_live = new TimeToLiveBuilder

  final val read_repair_chance = new ReadRepairChanceBuilder

  final val replicate_on_write = new ReplicateOnWriteBuilder

  final val gc_grace_seconds = new GcGraceSecondsBuilder

  final val bloom_filter_fp_chance = new BloomFilterFpChanceBuilder

  final val dclocal_read_repair_chance = new DcLocalReadRepairChanceBuilder

  final val comment  = new CommentClauseBuilder
}