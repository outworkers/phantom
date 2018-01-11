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
package com.outworkers.phantom.builder.query.bugs

import com.outworkers.phantom.dsl._

import scala.concurrent.Future

case class UserSchema(
  id: Int,
  firstName: String,
  lastName: String,
  dateOfBirth: LocalDate
)

abstract class UserSchemaTable extends Table[UserSchemaTable, UserSchema] {
  override def tableName: String = "users"

  object id extends IntColumn with PartitionKey
  object firstName extends StringColumn
  object lastName extends StringColumn
  object dateOfBirth extends LocalDateColumn

  def getUserId: Future[Option[Int]] = select(_.id).one()
}