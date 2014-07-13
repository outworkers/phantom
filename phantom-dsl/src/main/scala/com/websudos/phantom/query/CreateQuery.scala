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
