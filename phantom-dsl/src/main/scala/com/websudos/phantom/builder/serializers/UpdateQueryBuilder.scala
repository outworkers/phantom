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
 * - Explicit consent must be obtained from the copyright owner, Websudos Limited before any redistribution is made.
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
package com.websudos.phantom.builder.serializers

import com.websudos.phantom.builder.QueryBuilder.Utils
import com.websudos.phantom.builder.query.CQLQuery
import com.websudos.phantom.builder.syntax.CQLSyntax

private[builder] class UpdateQueryBuilder {

  def `if`(column: String, value: String): CQLQuery = {
    Utils.concat(column, CQLSyntax.Operators.eqs, value)
  }

  def onlyIf(clause: CQLQuery): CQLQuery = {
    CQLQuery(CQLSyntax.`if`).forcePad.append(clause)
  }

  private[this] def counterSetter(column: String, op: String, value: String): CQLQuery = {
    CQLQuery(column).forcePad.append(CQLSyntax.Symbols.`=`)
      .forcePad.append(column)
      .forcePad.append(op)
      .forcePad.append(value)
  }

  def increment(column: String, value: String): CQLQuery = {
    counterSetter(column, CQLSyntax.Symbols.+, value)
  }

  def decrement(column: String, value: String): CQLQuery = {
    counterSetter(column, CQLSyntax.Symbols.-, value)
  }

  def setTo(column: String, value: String): CQLQuery = {
    Utils.concat(column, CQLSyntax.Symbols.`=`, value)
  }

  def set(clause: CQLQuery): CQLQuery = {
    CQLQuery(CQLSyntax.set).forcePad.append(clause)
  }

  def where(condition: CQLQuery): CQLQuery = {
   Utils.operator(CQLSyntax.where, condition)
  }

  def and(condition: CQLQuery): CQLQuery = {
    Utils.operator(CQLSyntax.and, condition)
  }

  def clauses(clauses: List[CQLQuery], sep: String = " "): CQLQuery = {
    CQLQuery.empty.append(clauses.map(_.queryString).mkString(sep))
  }

  def chain(clauses: List[CQLQuery]): CQLQuery = {
    CQLQuery.empty.append(clauses.map(_.queryString).mkString(", "))
  }

  def clauses(op: String, clauses: List[CQLQuery]): CQLQuery = {
    CQLQuery(op)
      .forcePad
      .append(clauses.map(_.queryString).mkString(" "))
  }

  def usingPart(queries: List[CQLQuery]): CQLQuery = {
    clauses(CQLSyntax.using, queries)
  }


}

