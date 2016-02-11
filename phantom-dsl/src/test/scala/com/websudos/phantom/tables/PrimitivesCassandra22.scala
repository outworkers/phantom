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
 * - Explicit consent must be obtained from the copyright owner, Websudos Limited before any redistribution is made.
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
package com.websudos.phantom.tables

import com.websudos.phantom.builder.query.InsertQuery
import com.websudos.phantom.dsl._

case class PrimitiveCassandra22(
                      pkey: String,
                      short: Short,
                      byte: Byte
                    )

sealed class PrimitivesCassandra22 extends CassandraTable[ConcretePrimitivesCassandra22, PrimitiveCassandra22] {
  object pkey extends StringColumn(this) with PartitionKey[String]

  object short extends SmallIntColumn(this)

  object byte extends TinyIntColumn(this)

  override def fromRow(r: Row): PrimitiveCassandra22 = {
    PrimitiveCassandra22(
      pkey = pkey(r),
      short = short(r),
      byte = byte(r)
    )
  }
}

abstract class ConcretePrimitivesCassandra22 extends PrimitivesCassandra22 with RootConnector {

  override val tableName = "PrimitivesCassandra22"

  def store(row: PrimitiveCassandra22): InsertQuery.Default[ConcretePrimitivesCassandra22, PrimitiveCassandra22] = {
    insert
      .value(_.pkey, row.pkey)
      .value(_.short, row.short)
      .value(_.byte, row.byte)
  }
}
