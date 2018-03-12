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
package com.outworkers.phantom.thrift

import com.twitter.scrooge.{CompactThriftSerializer, ThriftStructCodec, ThriftStructSerializer}

trait ThriftProtocolWrapper[TP[X] <: ThriftStructSerializer[X], T <: ThriftStruct] {
  def protocol(codec: ThriftStructCodec[T]): TP[T]
}

object ThriftProtocolWrapper {
  implicit def default[T <: ThriftStruct]: ThriftProtocolWrapper[CompactThriftSerializer, T] = {
    new ThriftProtocolWrapper[CompactThriftSerializer, T] {
      override def protocol(
        tCodec: ThriftStructCodec[T]
      ): ThriftProtocolWrapper[CompactThriftSerializer, T] = {
        val serDes = CompactThriftSerializer[T](tCodec)
      }
    }
  }
}
