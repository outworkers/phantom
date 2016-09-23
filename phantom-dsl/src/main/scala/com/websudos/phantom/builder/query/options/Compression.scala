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
import com.websudos.phantom.builder.query.CQLQuery
import com.websudos.phantom.builder.syntax.CQLSyntax

sealed class CompressionStrategy(override val qb: CQLQuery) extends TablePropertyClause(qb) {

  def chunk_length_kb(unit: Int): CompressionStrategy = {
    new CompressionStrategy(QueryBuilder.Create.chunk_length_kb(qb, unit + "KB"))
  }

  def crc_check_chance(size: Double): CompressionStrategy = {
    new CompressionStrategy(QueryBuilder.Create.crc_check_chance(qb, size))
  }
}

private[phantom] trait CompressionStrategies {

  private[this] def strategy(strategy: String) = {
    CQLQuery(CQLSyntax.Symbols.`{`).forcePad
      .appendSingleQuote(CQLSyntax.CompressionOptions.sstable_compression)
      .append(CQLSyntax.Symbols.`:`)
      .forcePad.appendSingleQuote(strategy)
  }

  case object SnappyCompressor extends CompressionStrategy(strategy(CQLSyntax.CompressionStrategies.SnappyCompressor))
  case object LZ4Compressor extends CompressionStrategy(strategy(CQLSyntax.CompressionStrategies.LZ4Compressor))
  case object DeflateCompressor extends CompressionStrategy(strategy(CQLSyntax.CompressionStrategies.DeflateCompressor))
}



private[phantom] class CompressionBuilder extends TableProperty {
  def eqs(clause: CompressionStrategy): TablePropertyClause = {
    new TablePropertyClause(QueryBuilder.Create.compression(clause.qb))
  }
}