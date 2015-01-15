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

import scala.concurrent.{ Future => ScalaFuture }
import com.datastax.driver.core.{ ResultSet, Session }
import com.websudos.phantom.CassandraTable
import com.websudos.phantom.Implicits.context
import com.twitter.util.{ Future => TwitterFuture }

class CreateQuery[T <: CassandraTable[T, R], R](val table: T, query: String) extends CQLQuery[CreateQuery[T, R]] {
  val qb = null

  override def future()(implicit session: Session): ScalaFuture[ResultSet] = {
    if (table.createIndexes().isEmpty) {
      scalaQueryStringExecuteToFuture(table.schema())
    } else {
      scalaQueryStringExecuteToFuture(table.schema()) flatMap {
        _=> {
          ScalaFuture.sequence(table.createIndexes() map scalaQueryStringExecuteToFuture) map (_.head)
        }
      }
    }
  }

  override def execute()(implicit session: Session): TwitterFuture[ResultSet] = {
    if (table.createIndexes().isEmpty) {
      twitterQueryStringExecuteToFuture(table.schema())
    } else {
      twitterQueryStringExecuteToFuture(table.schema())  flatMap {
        _=> {
          TwitterFuture.collect(table.createIndexes() map twitterQueryStringExecuteToFuture) map (_.head)
        }
      }
    }
  }
}
