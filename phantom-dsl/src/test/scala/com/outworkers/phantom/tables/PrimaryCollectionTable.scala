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
package com.outworkers.phantom.tables

import com.outworkers.phantom.dsl._

case class PrimaryCollectionRecord(
  index: List[String],
  set: Set[String],
  map: Map[String, String],
  name: String,
  value: Int
)

abstract class PrimaryCollectionTable extends Table[PrimaryCollectionTable, PrimaryCollectionRecord] {
  object listIndex extends ListColumn[String] with PartitionKey
  object setCol extends SetColumn[String] with PrimaryKey
  object mapCol extends MapColumn[String, String] with PrimaryKey
  object name extends StringColumn with PrimaryKey
  object value extends IntColumn
}

case class NestedCollections(
  id: UUID,
  text: String,
  nestedList: List[List[String]],
  nestedListSet: List[Set[String]],
  props: Map[String, List[String]],
  doubleProps: Map[Set[String], List[String]]
)

abstract class NestedCollectionTable extends Table[
  NestedCollectionTable,
  NestedCollections
] {
  object id extends UUIDColumn with PartitionKey
  object text extends StringColumn
  object nestedList extends ListColumn[List[String]]
  object nestedListSet extends ListColumn[Set[String]]
  object props extends MapColumn[String, List[String]]
  object doubleProps extends MapColumn[Set[String], List[String]]
}
