/*
 * Copyright 2013-2017 Outworkers, Limited.
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
package com.outworkers.phantom.builder.query

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