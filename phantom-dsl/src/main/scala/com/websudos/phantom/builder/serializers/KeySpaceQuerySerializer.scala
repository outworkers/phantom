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
import com.websudos.phantom.builder.QueryBuilder.Utils
import com.websudos.phantom.builder.query.{CQLQuery, OptionPart}
import com.websudos.phantom.builder.syntax.CQLSyntax
import com.websudos.phantom.connectors.KeySpace

private object Strategies {
  final val networkTopology = "NetworkTopologyStrategy"
  final val simple = "SimpleStrategy"
  final val replicationFactor = "replication_factor"
  final val replication = "REPLICATION"
  final val durableWrites = "DURABLE_WRITES"
}

sealed class BuilderClause(val qb: CQLQuery)

sealed abstract class ReplicationStrategy[QT <: ReplicationStrategy[QT]](
  options: OptionPart = OptionPart.empty
) extends BuilderClause(options build CQLQuery.empty) {

  def instance(options: OptionPart): QT

  def option(key: String, value: String): QT = {
    val opt = Utils.option(CQLQuery.escape(key), CQLSyntax.Symbols.colon, CQLQuery.escape(value))
    instance(options append opt)
  }

  def option(key: String, value: Int): QT = {
    val opt = Utils.option(CQLQuery.escape(key), CQLSyntax.Symbols.colon, value.toString)
    instance(options append opt)
  }
}

sealed trait TopologyStrategies {

  private[this] def strategy(name: String): CQLQuery = {
    CQLQuery.empty
      .appendSingleQuote(CQLSyntax.CompactionOptions.`class`)
      .append(CQLSyntax.Symbols.colon).forcePad.appendSingleQuote(name)
  }

  sealed class NetworkTopologyStrategy(
    optionPart: OptionPart = OptionPart(strategy(Strategies.networkTopology))
  ) extends ReplicationStrategy[NetworkTopologyStrategy](optionPart) {

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
      option(center, factor)
    }

    override def instance(options: OptionPart): NetworkTopologyStrategy = {
      new NetworkTopologyStrategy(options)
    }
  }

  sealed class SimpleStrategy(options: OptionPart) extends ReplicationStrategy[SimpleStrategy](options) {

    def replication_factor(factor: Int): SimpleStrategy = {
      option(Strategies.replicationFactor, factor)
    }

    override def instance(options: OptionPart): SimpleStrategy = {
      new SimpleStrategy(options)
    }
  }

  object SimpleStrategy extends SimpleStrategy(OptionPart(strategy(Strategies.simple)))
  object NetworkTopologyStrategy extends NetworkTopologyStrategy(OptionPart(strategy(Strategies.networkTopology)))

  val replication = new {
    def eqs(strategy: ReplicationStrategy[_]): BuilderClause = {
      new BuilderClause(
        CQLQuery(Strategies.replication)
          .forcePad.append(CQLSyntax.eqs)
          .forcePad.append(strategy.qb)
      )
    }
  }

  val durable_writes = new {
    def eqs(clause: Boolean): BuilderClause = {
      new BuilderClause(CQLQuery(s"${Strategies.durableWrites} ${CQLSyntax.eqs} ${clause.toString}"))
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
