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
package com.outworkers.phantom.builder.query

import java.nio.ByteBuffer
import java.util.{Collections, Map => JMap}

import scala.collection.JavaConverters._
import com.datastax.driver.core._
import com.datastax.driver.core.policies.TokenAwarePolicy
import com.outworkers.phantom.Manager
import com.outworkers.phantom.builder.ops.TokenizerKey
import com.outworkers.phantom.builder.primitives.Primitive
import shapeless.{Generic, HList, HNil, Lazy, ::}

trait Modifier extends (Statement => Statement)

case class Payload(underlying: JMap[String, ByteBuffer]) {
  def isEmpty: Boolean = underlying.isEmpty

  def add(other: (String, ByteBuffer)): Payload = {
    val (key, value) = other
    underlying.put(key, value)
    Payload(underlying)
  }

  def add[T](other: (String, T))(
    implicit ev: Primitive[T],
    pv: ProtocolVersion
  ): Payload = {
    val (key, value) = other
    underlying.put(key, ev.serialize(value, pv))
    Payload(underlying)
  }
}

trait PayloadSerializer[HL <: HList] {
  def apply(input: HL): Seq[(String, ByteBuffer)]
}

object PayloadSerializer {

  def apply[HL <: HList](implicit ev: PayloadSerializer[HL]): PayloadSerializer[HL] = ev

  implicit val hNilSerializer: PayloadSerializer[HNil] = (_: HNil) => Seq.empty

  implicit def hconsSerializer[H, HL <: HList, A, B](
    implicit tpEv: H <:< (String, B),
    ev: Primitive[B],
    pv: ProtocolVersion,
    ps: Lazy[PayloadSerializer[HL]]
  ): PayloadSerializer[H :: HL] = (input: H :: HL) => {
    val (key, value): (String, B) = input.head
    Seq(key -> ev.serialize(value, pv)) ++ ps.value(input.tail)
  }
}

object Payload {
  def empty: Payload = new Payload(Collections.emptyMap())

  def apply(map: Map[String, ByteBuffer]): Payload = new Payload(map.asJava)

  def apply(tp: (String, ByteBuffer)): Payload = apply(Seq(tp).toMap)

  def apply(tp: (String, ByteBuffer)*): Payload = apply(tp.toMap)

  def apply[T](tp: (String, T))(implicit ev: Primitive[T], pv: ProtocolVersion): Payload = {
    val (key, value) = tp
    apply(Seq(key -> ev.serialize(value, pv)).toMap)
  }

  def apply[V1, HL <: HList](tp: V1)(
    implicit gen: Generic.Aux[V1, HL],
    pv: ProtocolVersion,
    ps: PayloadSerializer[HL]
  ): Payload = {
    apply(ps(gen to tp): _*)
  }
}

case class RoutingKeyModifier(
  tokens: List[TokenizerKey]
)(
  implicit session: Session
) extends (SimpleStatement => SimpleStatement) {
  override def apply(st: SimpleStatement): SimpleStatement = {

    val policy = session.getCluster.getConfiguration.getPolicies.getLoadBalancingPolicy

    if (policy.isInstanceOf[TokenAwarePolicy] && tokens.nonEmpty) {

      val routingKeys = tokens.map(_.apply(session))

      Manager.logger.debug(s"Routing key tokens found. Settings routing key to ${routingKeys.map(_.cql).mkString("(", ",", ")")}")

      st
        .setRoutingKey(routingKeys.map(_.bytes):_*)
        .setKeyspace(session.getLoggedKeyspace)
    } else {
      st
    }
  }
}

class ConsistencyLevelModifier(level: Option[ConsistencyLevel]) extends Modifier {
  override def apply(v1: Statement): Statement = {
    (level map v1.setConsistencyLevel).getOrElse(v1)
  }
}

class SerialConsistencyLevelModifier(level: Option[ConsistencyLevel]) extends Modifier {
  override def apply(v1: Statement): Statement = {
    (level map v1.setSerialConsistencyLevel).getOrElse(v1)
  }
}


class PayloadModifier(payload: Payload) extends Modifier {
  override def apply(v1: Statement): Statement = {
    if (payload.isEmpty) {
      v1
    } else {
      v1.setOutgoingPayload(payload.underlying)
    }
  }
}



class PagingStateModifier(level: Option[PagingState]) extends Modifier {
  override def apply(v1: Statement): Statement = {
    (level map v1.setPagingState).getOrElse(v1)
  }
}

class EnableTracingModifier(level: Option[Boolean]) extends Modifier {
  override def apply(v1: Statement): Statement = {
    level match {
      case Some(true) => v1.enableTracing()
      case Some(false) => v1.disableTracing()
      case None => v1
    }
  }
}

class FetchSizeModifier(level: Option[Int]) extends Modifier {
  override def apply(v1: Statement): Statement = {
    (level map v1.setFetchSize).getOrElse(v1)
  }
}

case class QueryOptions(
  consistencyLevel: Option[ConsistencyLevel],
  serialConsistencyLevel: Option[ConsistencyLevel],
  pagingState: Option[PagingState] = None,
  enableTracing: Option[Boolean] = None,
  fetchSize: Option[Int] = None,
  outgoingPayload: Payload = Payload.empty
) {

  def apply(st: Statement): Statement = {
    val applier = List[Statement => Statement](
      new ConsistencyLevelModifier(consistencyLevel),
      new SerialConsistencyLevelModifier(serialConsistencyLevel),
      new PagingStateModifier(pagingState),
      new EnableTracingModifier(enableTracing),
      new FetchSizeModifier(fetchSize),
      new PayloadModifier(outgoingPayload)
    ) reduce(_ andThen _)

    applier(st)
  }

  def options: com.datastax.driver.core.QueryOptions = {
    val opt = new com.datastax.driver.core.QueryOptions()

    consistencyLevel map opt.setConsistencyLevel
    serialConsistencyLevel map opt.setSerialConsistencyLevel
    fetchSize map opt.setFetchSize

    opt
  }

  def outgoingPayload_=(payload: Payload): QueryOptions = {
    this.copy(outgoingPayload = payload)
  }

  def consistencyLevel_=(level: ConsistencyLevel): QueryOptions = {
    this.copy(consistencyLevel = Some(level))
  }

  def serialConsistencyLevel_=(level: ConsistencyLevel): QueryOptions = {
    this.copy(serialConsistencyLevel = Some(level))
  }

  def enableTracing_=(flag: Boolean): QueryOptions = {
    this.copy(enableTracing = Some(flag))
  }

  def fetchSize_=(size: Int): QueryOptions = {
    this.copy(fetchSize = Some(size))
  }
}

object QueryOptions {
  def empty: QueryOptions = {
    new QueryOptions(
      consistencyLevel = None,
      serialConsistencyLevel = None,
      pagingState = None,
      enableTracing = None,
      fetchSize = None
    )
  }
}