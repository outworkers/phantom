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
import com.outworkers.phantom.builder.query.OptionPart
import com.outworkers.phantom.builder.query.engine.CQLQuery
import com.outworkers.phantom.builder.syntax.CQLSyntax

sealed abstract class CompressionStrategy[
  CS <: CompressionStrategy[CS]
](val options: OptionPart) extends ClauseBuilder[CS] {

  def chunk_length_kb(unit: Int): CompressionStrategy[CS] = {
    option(
      CQLSyntax.CompressionOptions.chunk_length_kb,
      unit + "KB"
    )
  }

  def crc_check_chance(size: Double): CompressionStrategy[CS] = {
    option(CQLSyntax.CompressionOptions.crc_check_chance, size.toString)
  }
}

private[phantom] trait CompressionStrategies {

  private[this] def strategy(strategy: String): OptionPart = {
    val qb = CQLQuery.empty
      .appendSingleQuote(CQLSyntax.CompressionOptions.sstable_compression)
      .append(CQLSyntax.Symbols.colon)
      .forcePad.appendSingleQuote(strategy)

    OptionPart(qb)
  }

  class SnappyCompressor extends CompressionStrategy[SnappyCompressor](
    strategy(CQLSyntax.CompressionStrategies.SnappyCompressor)
  ) {
    override protected[this] def instance(opts: OptionPart): SnappyCompressor = new SnappyCompressor {
      override val options: OptionPart = opts
    }
  }

  object SnappyCompressor extends SnappyCompressor

  class LZ4Compressor extends CompressionStrategy[LZ4Compressor](
    strategy(CQLSyntax.CompressionStrategies.LZ4Compressor)
  ) {
    override protected[this] def instance(opts: OptionPart): LZ4Compressor = new LZ4Compressor {
      override val options: OptionPart = opts
    }
  }

  object LZ4Compressor extends LZ4Compressor

  class DeflateCompressor extends CompressionStrategy[DeflateCompressor](
    strategy(CQLSyntax.CompressionStrategies.DeflateCompressor)
  ) {
    override protected[this] def instance(opts: OptionPart): DeflateCompressor = new DeflateCompressor {
      override val options: OptionPart = opts
    }
  }

  object DeflateCompressor extends DeflateCompressor
}

private[phantom] class CompressionBuilder extends TableProperty {
  def eqs(clause: CompressionStrategy[_]): TablePropertyClause = {
    new TablePropertyClause {
      override def qb: CQLQuery = QueryBuilder.Create.compression(clause.qb)
    }
  }
}