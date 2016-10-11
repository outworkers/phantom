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