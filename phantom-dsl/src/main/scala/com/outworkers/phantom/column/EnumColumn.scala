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

import com.datastax.driver.core.Row
import com.outworkers.phantom.CassandraTable
import com.outworkers.phantom.builder.query.CQLQuery
import com.outworkers.phantom.builder.syntax.CQLSyntax

import scala.util.{Failure, Success, Try}

class EnumColumn[
  Owner <: CassandraTable[Owner, Record],
  Record,
  EnumType <: Enumeration
](table: CassandraTable[Owner, Record], enum: EnumType)
  extends Column[Owner, Record, EnumType#Value](table) {

  def cassandraType: String = CQLSyntax.Types.Text

  def parse(r: Row): Try[EnumType#Value] = {
    val enumConstant = r.getString(name)

    enum.values.find(_.toString == enumConstant) match {
      case Some(value) => Success(value)
      case None => Failure(new Exception(s"Enumeration ${enum.toString()} doesn't contain value $enumConstant"))
    }
  }

  override def asCql(v: EnumType#Value): String = CQLQuery.empty.singleQuote(v.toString)
}

class OptionalEnumColumn[
  Owner <: CassandraTable[Owner, Record],
  Record,
  EnumType <: Enumeration
](table: CassandraTable[Owner, Record], enum: EnumType)
  extends OptionalColumn[Owner, Record, EnumType#Value](table) {

  def cassandraType: String = CQLSyntax.Types.Text

  def optional(r: Row): Try[EnumType#Value] = {

    val enumConstant = r.getString(name)

    enum.values.find(_.toString == enumConstant) match {
      case Some(value) => Success(value)
      case None => Failure(new Exception(s"Enumeration ${enum.toString()} doesn't contain value $enumConstant"))
    }
  }

  override def asCql(v: Option[EnumType#Value]): String = {
    v.map(item => CQLQuery.empty.singleQuote(item.toString)).orNull
  }

}
