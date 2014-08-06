/*
 * Copyright 2013 websudos ltd.
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
package com.websudos.phantom.tables

import com.datastax.driver.core.Row
import com.newzly.util.testing.Sampler
import com.websudos.phantom.Implicits._
import com.websudos.phantom.helper.{ModelSampler, TestSampler}
import com.websudos.phantom.{CassandraTable, PhantomCassandraConnector}

case class MyTestRow(
  key: String,
  optionA: Option[Int],
  stringlist: List[String]
)

object MyTestRow extends ModelSampler[MyTestRow] {
  def sample: MyTestRow = MyTestRow(
    Sampler.getARandomString,
    Some(Sampler.getARandomInteger()),
    List.range(0, 20).map(x => Sampler.getARandomString)
  )
}

sealed class MyTest extends CassandraTable[MyTest, MyTestRow] {
  def fromRow(r: Row): MyTestRow = {
    MyTestRow(key(r), optionA(r), stringlist(r))
  }

  object key extends StringColumn(this) with PartitionKey[String]

  object stringlist extends ListColumn[MyTest, MyTestRow, String](this)

  object optionA extends OptionalIntColumn(this)

}

object MyTest extends MyTest with TestSampler[MyTest, MyTestRow] with PhantomCassandraConnector {

  override val tableName = "mytest"

}


