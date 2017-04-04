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
package com.outworkers.phantom.column

import com.outworkers.phantom.{ CassandraTable, Row }

import scala.util.{Failure, Success, Try}

abstract class Column[
  Owner <: CassandraTable[Owner, Record],
  Record,
  T
](val table: CassandraTable[Owner, Record]) extends AbstractColumn[T] {

  def parse(r: Row): Try[T]

  def apply(r: Row): T = parse(r) match {
    case Success(value) => value
    case Failure(ex) =>
      table.logger.error(s"Unable to parse value for column $name from row", ex)
      throw ex
  }
}
