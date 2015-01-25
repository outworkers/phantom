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
package com.websudos.phantom.query

import com.datastax.driver.core.querybuilder.{QueryBuilder, Delete}
import com.websudos.phantom.CassandraTable

class DeleteQuery[T <: CassandraTable[T, R], R](table: T, val qb: Delete)
  extends CQLQuery[DeleteQuery[T, R]] with BatchableQuery[DeleteQuery[T, R]] {

  def where[RR](condition: T => QueryCondition): DeleteWhere[T, R] = {
    new DeleteWhere[T, R](table, qb.where(condition(table).clause))
  }

  def timestamp(l: Long): DeleteQuery[T, R] = {
    qb.using(QueryBuilder.timestamp(l))
    this
  }
}

class DeleteWhere[T <: CassandraTable[T, R], R](table: T, val qb: Delete.Where)
  extends CQLQuery[DeleteWhere[T, R]] with BatchableQuery[DeleteWhere[T, R]] {

  def and[RR](condition: T => QueryCondition): DeleteWhere[T, R] = {
    new DeleteWhere[T, R](table, qb.and(condition(table).clause))
  }

  def timestamp(l: Long): DeleteWhere[T, R] = {
    qb.using(QueryBuilder.timestamp(l))
    this
  }
}
