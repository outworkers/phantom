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

import com.websudos.phantom.helper.ModelSampler
import com.websudos.util.testing.Sampler
import com.websudos.phantom.zookeeper.DefaultZookeeperConnector

case class SimpleStringClass(something: String)

object SimpleStringClass extends ModelSampler[SimpleStringClass] {
  def sample: SimpleStringClass = SimpleStringClass(Sampler.getARandomString)
}

case class SimpleMapOfStringsClass(something: Map[String, Int])

object SimpleMapOfStringsClass extends ModelSampler[SimpleMapOfStringsClass] {
  def sample: SimpleMapOfStringsClass = SimpleMapOfStringsClass(Map(
    Sampler.getARandomString -> Sampler.getARandomInteger(),
    Sampler.getARandomString -> Sampler.getARandomInteger(),
    Sampler.getARandomString -> Sampler.getARandomInteger(),
    Sampler.getARandomString -> Sampler.getARandomInteger(),
    Sampler.getARandomString -> Sampler.getARandomInteger()
  ))
}

case class TestList(key: String, l: List[String])

object TestList extends ModelSampler[TestList]  with DefaultZookeeperConnector {

  val keySpace = "phantom"

  def sample: TestList = TestList(
    Sampler.getARandomString,
    List.range(0, 20).map(x => Sampler.getARandomString)
  )
}

case class SimpleStringModel(something: String) extends ModelSampler[SimpleStringModel] {
  def sample: SimpleStringModel = SimpleStringModel(Sampler.getARandomString)
}

case class TestRow2(
  key: String,
  optionalInt: Option[Int],
  simpleMapOfString: SimpleMapOfStringsClass,
  optionalSimpleMapOfString: Option[SimpleMapOfStringsClass],
  mapOfStringToCaseClass: Map[String, SimpleMapOfStringsClass]
)

object TestRow2 extends ModelSampler[TestRow2]  with DefaultZookeeperConnector {
  val keySpace = "phantom"
  def sample = sample(5)
  def sample(limit: Int = 5): TestRow2 = {
    TestRow2(
      Sampler.getARandomString,
      Some(Sampler.getARandomInteger()),
      SimpleMapOfStringsClass.sample,
      Some(SimpleMapOfStringsClass.sample),
      List.range(0, limit).map(x => { x.toString -> SimpleMapOfStringsClass.sample}).toMap
    )
  }
}
