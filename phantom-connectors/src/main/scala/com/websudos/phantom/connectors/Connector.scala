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
package com.websudos.phantom.connectors

import com.datastax.driver.core.{ProtocolVersion, Session}

/* Trait to be mixed into the table classes that extend
 * phantom's CassandraTable.
 *
 * The main purpose of this trait is to provide an implicit
 * `Session` to the query and update logic in your table
 * class.
 *
 * The most convenient approach is to mix in this abstract
 * base trait into your abstract table classes, so that
 * the implicit `Session` is available in your table
 * implementation and then instantiate a sub-class
 * that mixes in the concrete trait from a `KeySpace`
 * instance.
 *
 * {{{
 * // table class:
 * abstract class Foos extends CassandraTable[Foos, Foo] with Connector {
 *   [...]
 * }
 *
 * // concrete instance:
 * val hosts = Seq("35.0.0.1", "35.0.0.2")
 * val keySpace = ContactPoints(hosts).keySpace("myApp")
 *
 * object foos extends Foos with keySpace.Connector
 * }}}
 */
trait Connector {

  /**
   * The name of the keyspace this Connector should use.
   */
  def keySpace: String

  /**
   * The provider for the session instance.
   */
  def provider: SessionProvider

  /**
   * The implicit Session instance for the
   * query and update operations in phantom
   * table implementations.
   */
  implicit lazy val session: Session = provider.session

}
