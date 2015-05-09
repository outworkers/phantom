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
package com.websudos.phantom.keys

import com.websudos.phantom.column.AbstractColumn

private[phantom] trait Undroppable
private[phantom] trait Unmodifiable
private[phantom] trait Indexed

private[phantom] trait Key[ValueType, KeyType <: Key[ValueType, KeyType]] {
  self: AbstractColumn[ValueType] =>
}

trait PrimaryKey[ValueType] extends Key[ValueType, PrimaryKey[ValueType]] with Unmodifiable with Indexed with Undroppable {
  self: AbstractColumn[ValueType] =>
  override val isPrimary = true
}

trait PartitionKey[ValueType] extends Key[ValueType, PartitionKey[ValueType]] with Unmodifiable with Indexed with Undroppable {
  self: AbstractColumn[ValueType] =>
  override val isPartitionKey = true
  override val isPrimary = true
}

/**
 * A trait mixable into Column definitions to allow storing them as keys.
 */
trait Index[ValueType] extends Key[ValueType, Index[ValueType]] with Indexed with Undroppable {
  self: AbstractColumn[ValueType] => override val isSecondaryKey = true
}
