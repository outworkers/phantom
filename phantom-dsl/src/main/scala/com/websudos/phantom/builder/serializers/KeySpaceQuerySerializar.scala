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
package com.websudos.phantom.builder.serializers

import com.websudos.phantom.builder.QueryBuilder
import com.websudos.phantom.builder.query.CQLQuery
import com.websudos.phantom.builder.syntax.CQLSyntax
import com.websudos.phantom.connectors.KeySpace

private object Strategies {
  final val NetworkTopologyStrategy = "NetworkTopologyStrategy"
  final val SimpleStrategy = "SimpleStrategy"
  final val ReplicationFactor = "replication_factor"
  final val Replication = "REPLICATION"
  final val DurableWrites = "DURABLE_WRITES"
}

sealed class BuilderClause(val qb: CQLQuery)

sealed class ReplicationStrategy(override val qb: CQLQuery) extends BuilderClause(qb) {

  def option(key: String, value: String): CQLQuery = {
    CQLQuery(CQLQuery.escape(key))
      .forcePad.append(":")
      .pad.append(CQLQuery.escape(value))
  }

  def option(key: String, value: Int): CQLQuery = {
    CQLQuery(CQLQuery.escape(key)).forcePad.append(":").pad.append(value.toString)
  }
}

sealed trait TopologyStrategies {

  private[this] def strategy(name: String): CQLQuery = {
    CQLQuery(CQLSyntax.Symbols.`{`)
      .appendSingleQuote(CQLSyntax.CompactionOptions.`class`)
      .append(":").forcePad.appendSingleQuote(name)
      .append(CQLSyntax.Symbols.`}`)
  }

  sealed class NetworkTopologyStrategy(override val qb: CQLQuery = strategy(Strategies.NetworkTopologyStrategy)) extends
    ReplicationStrategy(qb) {

    /**
      * Utility method that allows users to specify the replication factor for every data center
      * in the network topology. The center should be a string value containing the name of
      * a data center and the factor should be the replication factor desired.
      *
      * Example:
      *
      * {{{
      *   NetworkTopologyStrategy.data_center("dc1": 2).data_center("dc2": 3)
      * }}}
      *
      * @param center The name of the data center to specify the replication factor for.
      * @param factor The int value of the replication factor to use/
      * @return A serializable network topology strategy that can be encoded.
      */
    def data_center(center: String, factor: Int): NetworkTopologyStrategy = {
      new NetworkTopologyStrategy(
        CQLQuery(qb.queryString.dropRight(1))
          .append(option(center, factor))
          .append(CQLSyntax.Symbols.`}`)
      )
    }
  }


  sealed class SimpleStrategy(override val qb: CQLQuery) extends
    ReplicationStrategy(qb) {

    def replication_factor(factor: Int): SimpleStrategy = {
      new SimpleStrategy(
        CQLQuery(qb.queryString.dropRight(1))
          .append(option(Strategies.ReplicationFactor, factor))
          .append(CQLSyntax.Symbols.`}`)
      )
    }
  }

  case object SimpleStrategy extends SimpleStrategy(strategy(Strategies.SimpleStrategy))
  case object NetworkTopologyStrategy extends NetworkTopologyStrategy(strategy(Strategies.NetworkTopologyStrategy))

  object replication {
    def eqs(strategy: ReplicationStrategy): BuilderClause = {
      new BuilderClause(
        CQLQuery(Strategies.Replication)
          .forcePad.append(CQLSyntax.eqs)
          .forcePad.append(strategy.qb)
      )
    }
  }

  object durable_writes {
    def eqs(clause: Boolean): BuilderClause = {
      new BuilderClause(CQLQuery(Strategies.DurableWrites + " = " + clause.toString))
    }
  }
}


sealed class KeySpaceSerializer(val keySpace: KeySpace, val qb: CQLQuery = CQLQuery.empty) {

  def `with`(clause: BuilderClause): KeySpaceSerializer = {
    new KeySpaceSerializer(keySpace, QueryBuilder.Alter.`with`(qb, clause.qb))
  }

  def and(clause: BuilderClause): KeySpaceSerializer = {
    new KeySpaceSerializer(keySpace, QueryBuilder.Where.and(qb, clause.qb))
  }
}

class RootSerializer(val keySpace: KeySpace) {
  def ifNotExists(): KeySpaceSerializer = {
    new KeySpaceSerializer(keySpace, CQLQuery(s"CREATE KEYSPACE IF NOT EXISTS ${keySpace.name}"))
  }

  protected[phantom] def default(): KeySpaceSerializer = {
    new KeySpaceSerializer(keySpace, CQLQuery(s"CREATE KEYSPACE ${keySpace.name}"))
  }
}

object KeySpaceSerializer {
  def apply(name: String): RootSerializer = new RootSerializer(KeySpace(name))

  def apply(keySpace: KeySpace): RootSerializer = new RootSerializer(keySpace)
}

private[phantom] trait KeySpaceConstruction extends TopologyStrategies {
  implicit def rootSerializerToKeySpaceSerializer(
    serializer: RootSerializer
  ): KeySpaceSerializer = serializer.default()
}
