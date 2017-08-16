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

package com.outworkers.phantom.builder.query.execution

import com.datastax.driver.core.PagingState
import com.outworkers.phantom.ResultSet

trait RecordResult[R] {

  def result: ResultSet

  def pagingState: PagingState = result.getExecutionInfo.getPagingState

  def state: Option[PagingState] = Option(result.getExecutionInfo.getPagingState)
}

case class ListResult[R](records: List[R], result: ResultSet) extends RecordResult[R]

object ListResult {
  def apply[R](res: ResultSet, records: List[R]): ListResult[R] = ListResult(records, res)

  def apply[R](rec: (ResultSet, List[R])): ListResult[R] = apply(rec._2, rec._1)
}

case class IteratorResult[R](records: Iterator[R], result: ResultSet) extends RecordResult[R]