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

case class TestRow(
  key: String,
  list: List[String],
  setText: Set[String],
  mapTextToText: Map[String, String],
  setInt: Set[Int],
  mapIntToText: Map[Int, String],
  mapIntToInt: Map[Int, Int]
)

abstract class TestTable extends Table[TestTable, TestRow] {

  object key extends StringColumn with PartitionKey

  object list extends ListColumn[String]

  object setText extends SetColumn[String]

  object mapTextToText extends MapColumn[String, String]

  object setInt extends SetColumn[Int]

  object mapIntToText extends MapColumn[Int, String]

  object mapIntToInt extends MapColumn[Int, Int]
}
