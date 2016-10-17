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

package com.outworkers.phantom.thrift

import com.outworkers.phantom.CassandraTable

package object columns {
  type ThriftStruct = com.twitter.scrooge.ThriftStruct
  type ThriftColumn[T <: CassandraTable[T, R], R, Model <: ThriftStruct] = com.outworkers.phantom.thrift.columns.ThriftColumn[T, R, Model]
  type ThriftSetColumn[T <: CassandraTable[T, R], R, Model <: ThriftStruct] = com.outworkers.phantom.thrift.columns.ThriftSetColumn[T, R, Model]
  type ThriftListColumn[T <: CassandraTable[T, R], R, Model <: ThriftStruct] = com.outworkers.phantom.thrift.columns.ThriftListColumn[T, R, Model]
  type ThriftMapColumn[T <: CassandraTable[T, R], R, KeyType, Model <: ThriftStruct] = com.outworkers.phantom.thrift.columns.ThriftMapColumn[T, R, KeyType, Model]

  type OptionalThriftColumn[T <: CassandraTable[T, R], R, Model <: ThriftStruct] = com.outworkers.phantom.thrift.columns.OptionalThriftColumn[T, R, Model]

  type ThriftPrimitive[T <: ThriftStruct] = RootThriftPrimitive[T]
}



