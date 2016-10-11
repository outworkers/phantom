/*
 * Copyright 2013-2015 Websudos, Limited.
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * - Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 *
 * - Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *
 * - Explicit consent must be obtained from the copyright owner, Outworkers Limited before any redistribution is made.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package com.websudos.phantom.column

import com.datastax.driver.core.Row
import com.websudos.phantom.CassandraTable
import com.websudos.phantom.builder.query.CQLQuery
import com.websudos.phantom.builder.syntax.CQLSyntax

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
