/*
 * Copyright 2013 - 2019 Outworkers Ltd.
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

import com.outworkers.phantom.dsl._

import scala.concurrent.Future

case class OrgHierarchy(
  nodeId: String,
  nodeName: String,
  orgId: Option[String],
  orgName: Option[String],
  siteId: Option[String],
  siteName: Option[String],
  siteAddress: Option[String],
  bssId: Option[String],
  nodeHw: Option[String]
)

/**
  * Table structure to isolate prepared statement bug reported by Verizon.
  * Prepared statements should re-attach to clusters with some kind of exponential retry
  * strategy and leverage internals.
  */
abstract class OrgHierarchyByNode extends Table[OrgHierarchyByNode, OrgHierarchy] {

  def findById(nodeId: String): Future[Option[OrgHierarchy]] =
    getOrgQuery.flatMap(_.bind(nodeId).one())

  override def tableName: String = "orghierarchy_by_nodeid"

  object nodeId extends StringColumn with PartitionKey

  object nodeName extends OptionalStringColumn

  object orgId extends OptionalStringColumn

  object orgName extends OptionalStringColumn

  object siteId extends OptionalStringColumn

  object siteName extends OptionalStringColumn

  object siteAddress extends OptionalStringColumn

  object bssId extends OptionalStringColumn

  object nodeHw extends OptionalStringColumn

  lazy val getOrgQuery = select
    .where(_.nodeId eqs ?)
    .consistencyLevel_=(ConsistencyLevel.ONE)
    .prepareAsync()

}
