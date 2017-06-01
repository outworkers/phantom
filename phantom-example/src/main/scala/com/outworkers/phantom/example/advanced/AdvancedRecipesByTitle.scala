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
package com.outworkers.phantom.example.advanced

import java.util.UUID

import com.datastax.driver.core.{ResultSet, Row}
import com.outworkers.phantom.connectors.RootConnector
import com.outworkers.phantom.dsl._

import scala.concurrent.{Future => ScalaFuture}

// Now you want to enable querying Recipes by author.
// Because of the massive performance overhead of filtering,
// you can't really use a SecondaryKey for multi-billion record databases.

// Instead, you create mapping tables and ensure consistency from the application level.
// This will illustrate just how easy it is to do that with com.outworkers.phantom.
abstract class AdvancedRecipesByTitle extends Table[AdvancedRecipesByTitle, (String, UUID)] {

  // In this table, the author will be PrimaryKey and PartitionKey.
  object title extends StringColumn with PartitionKey

  // The id is just another normal field.
  object id extends UUIDColumn

  override lazy val tableName = "recipes_by_title"

  // now you can have the tile in a where clause
  // without the performance impact of a secondary index.
  def findRecipeByTitle(title: String): ScalaFuture[Option[(String, UUID)]] = {
    select.where(_.title eqs title).one()
  }
}

object AdvancedRecipesByTitle {
  {
    final class anon$macro$2 extends _root_.com.outworkers.phantom.macros.TableHelper[com.outworkers.phantom.example.advanced.AdvancedRecipesByTitle, (String, java.util.UUID)] {
      type Repr = shapeless.::[(String, java.util.UUID),shapeless.HNil];
      def tableName: _root_.java.lang.String = com.outworkers.phantom.NamingStrategy.identityStrategy.inferName("advancedRecipesByTitle");
      def store(table: com.outworkers.phantom.example.advanced.AdvancedRecipesByTitle, input: shapeless.::[(String, java.util.UUID),shapeless.HNil])(implicit space: _root_.com.outworkers.phantom.connectors.KeySpace): _root_.com.outworkers.phantom.builder.query.InsertQuery.Default[com.outworkers.phantom.example.advanced.AdvancedRecipesByTitle, (String, java.util.UUID)] = table.insert.values(_root_.com.outworkers.phantom.builder.query.engine.CQLQuery(table.title.name).->(_root_.com.outworkers.phantom.builder.query.engine.CQLQuery(table.title.asCql(input.apply(_root_.shapeless.Nat.apply(0))._1))), _root_.com.outworkers.phantom.builder.query.engine.CQLQuery(table.id.name).->(_root_.com.outworkers.phantom.builder.query.engine.CQLQuery(table.id.asCql(input.apply(_root_.shapeless.Nat.apply(0))._2))));
      def tableKey(table: com.outworkers.phantom.example.advanced.AdvancedRecipesByTitle): _root_.java.lang.String = _root_.com.outworkers.phantom.builder.QueryBuilder.Create.primaryKey(_root_.scala.collection.immutable.List[(_root_.com.outworkers.phantom.column.AbstractColumn[_$1] forSome {
        type _$1
      })](table.title).map(((x$2) => x$2.name)), _root_.scala.collection.immutable.List[(_root_.com.outworkers.phantom.column.AbstractColumn[_$1] forSome {
        type _$1
      })]().map(((x$3) => x$3.name))).queryString;
      def fromRow(table: com.outworkers.phantom.example.advanced.AdvancedRecipesByTitle, row: _root_.com.outworkers.phantom.Row): (String, java.util.UUID) = new (String, java.util.UUID)(table.title.apply(row), table.id.apply(row));
      def fields(table: com.outworkers.phantom.example.advanced.AdvancedRecipesByTitle): scala.collection.immutable.Seq[(_root_.com.outworkers.phantom.column.AbstractColumn[_$1] forSome {
        type _$1
      })] = scala.collection.immutable.Seq.apply[(_root_.com.outworkers.phantom.column.AbstractColumn[_$1] forSome {
        type _$1
      })](table.instance.title, table.instance.id);
      def debug: _root_.com.outworkers.phantom.macros.Debugger = new _root_.com.outworkers.phantom.macros.Debugger("record: (String, java.util.UUID)", _root_.scala.collection.immutable.Map.apply[String, String](_root_.scala.Tuple2("_1".+(":").+("String"), "_1".+(":").+("String")), _root_.scala.Tuple2("_2".+(":").+("java.util.UUID"), "_2".+(":").+("java.util.UUID"))), """rec._1 -> table.title | String
rec._2 -> table.id | java.util.UUID""")
    };
    ((new anon$macro$2()): _root_.com.outworkers.phantom.macros.TableHelper.Aux[com.outworkers.phantom.example.advanced.AdvancedRecipesByTitle, (String, java.util.UUID), shapeless.::[(String, java.util.UUID),shapeless.HNil]])
  }

}