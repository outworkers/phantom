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
package com.outworkers.phantom.builder.serializers

import com.outworkers.phantom.builder.QueryBuilder
import com.outworkers.phantom.builder.QueryBuilder.Utils
import com.outworkers.phantom.builder.query.engine.CQLQuery
import com.outworkers.phantom.builder.query.OptionPart
import com.outworkers.phantom.builder.syntax.CQLSyntax
import com.outworkers.phantom.connectors.{KeySpace, KeySpaceCQLQuery}

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
      .appendSingleQuote(CQLSyntax.CompactionOptions.clz)
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


sealed class KeySpaceSerializer(
  val keySpace: KeySpace,
  val qb: CQLQuery = CQLQuery.empty
) extends KeySpaceCQLQuery {

  def `with`(clause: BuilderClause): KeySpaceSerializer = {
    new KeySpaceSerializer(keySpace, QueryBuilder.Alter.option(qb, clause.qb))
  }

  def and(clause: BuilderClause): KeySpaceSerializer = {
    new KeySpaceSerializer(keySpace, QueryBuilder.Where.and(qb, clause.qb))
  }

  override def queryString: String = qb.queryString

  override def keyspace: String = keySpace.name
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
