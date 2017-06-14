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
package com.outworkers.phantom.builder

import com.outworkers.phantom.builder.query.engine.CQLQuery
import com.outworkers.phantom.builder.serializers._
import com.outworkers.phantom.builder.syntax.CQLSyntax
import com.outworkers.phantom.connectors.KeySpace

case class QueryBuilderConfig(caseSensitiveTables: Boolean)

object QueryBuilderConfig {
  final val Default = new QueryBuilderConfig(false)
}

abstract class QueryBuilder(val config: QueryBuilderConfig = QueryBuilderConfig.Default) {

  case object Create extends CreateTableBuilder

  case object Delete extends DeleteQueryBuilder

  case object Update extends UpdateQueryBuilder

  case object Collections extends CollectionModifiers(this)

  case object Where extends IndexModifiers

  case object SASI extends SASIQueryBuilder

  case object Select extends SelectQueryBuilder

  case object Batch extends BatchQueryBuilder

  case object Utils extends Utils

  case object Alter extends AlterQueryBuilder

  case object Insert extends InsertQueryBuilder

  def truncate(table: String): CQLQuery = {
    CQLQuery(CQLSyntax.truncate).forcePad.append(table)
  }

  def using(qb: CQLQuery): CQLQuery = {
    qb.pad.append(CQLSyntax.using)
  }

  def ttl(seconds: String): CQLQuery = {
    CQLQuery(CQLSyntax.CreateOptions.ttl).forcePad.append(seconds)
  }

  /**
    * Produces a timestamp clause that should be appended to a UsingPart.
    * @param unixTimestamp The milliseconds since EPOCH long value of a timestamp.
    * @return A CQLQuery wrapping the USING clause.
    */
  def timestamp(unixTimestamp: Long): CQLQuery = {
    CQLQuery(CQLSyntax.timestamp).forcePad.append(unixTimestamp.toString)
  }

  def consistencyLevel(qb: CQLQuery, level: String): CQLQuery = {
    using(qb).pad.append(CQLSyntax.consistency).forcePad.append(level)
  }

  def consistencyLevel(level: String): CQLQuery = {
    CQLQuery(CQLSyntax.consistency).forcePad.append(level)
  }

  def tableDef(tableName: String): CQLQuery = {
    if (config.caseSensitiveTables) {
      CQLQuery(CQLQuery.escape(tableName))
    } else {
      CQLQuery(tableName)
    }
  }

  def keyspace(space: String, tableQuery: CQLQuery): CQLQuery = {
    keyspace(space, tableQuery.queryString)
  }

  def keyspace(keySpace: String, table: String): CQLQuery = {
    if (table.startsWith(keySpace + ".")) {
      tableDef(table)
    }  else {
      tableDef(table).prepend(s"$keySpace.")
    }
  }

  def limit(value: String): CQLQuery = {
    CQLQuery(CQLSyntax.limit).forcePad.append(value.toString)
  }

  def keyspace(space: String): RootSerializer = KeySpaceSerializer(space)

  def keyspace(space: KeySpace): RootSerializer = KeySpaceSerializer(space)

}

object QueryBuilder extends QueryBuilder(QueryBuilderConfig.Default)
