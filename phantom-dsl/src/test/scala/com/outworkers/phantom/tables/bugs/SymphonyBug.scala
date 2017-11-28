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
package com.outworkers.phantom.tables.bugs

import java.nio.ByteBuffer

import com.outworkers.phantom.dsl._

import scala.concurrent.Future

case class Shard(id: String, shardNo: Int, accountId: String, size: Int, data: ByteBuffer)

abstract class ShardModel extends Table[ShardModel, Shard] {

  override def tableName: String = "shard"

  object id extends StringColumn with PartitionKey
  object shardNo extends IntColumn with PartitionKey

  object accountId extends StringColumn with ClusteringOrder
  object size extends IntColumn

  object data extends BlobColumn

  def find(id: String, shardNo: Int): Future[Array[Byte]] = {
    select(_.data).where(_.id eqs id).and(_.shardNo eqs shardNo).one()
      .map(_.map(_.array()).getOrElse(Array.emptyByteArray))
  }

  def remove(id: String, accountId: String, shards: Int): Future[Boolean] = {
    val f = (1 to shards).map(x => delete.where(_.id eqs id).and(_.shardNo eqs x)
      .and(_.accountId eqs accountId)
      .future.map(_.wasApplied())).toList
    Future.sequence(f) map (_.forall(true ==))
  }
}

class MediaDatabase(override val connector: CassandraConnection) extends Database[MediaDatabase](connector) {
  object ShardModel extends ShardModel with connector.Connector
}