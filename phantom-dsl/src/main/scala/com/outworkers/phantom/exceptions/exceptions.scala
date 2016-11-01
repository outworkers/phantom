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
package com.outworkers.phantom.exceptions

import scala.util.control.NoStackTrace

case class InvalidClusteringKeyException(table: String) extends
  RuntimeException(s"Table $table: When using CLUSTERING ORDER all PrimaryKey" +
    s" definitions must become a ClusteringKey definition and specify order."
  ) with NoStackTrace

case class InvalidPrimaryKeyException(
  table: String
) extends RuntimeException(s"You need to define at least one PartitionKey for the table $table") with NoStackTrace

case class InvalidTableException(msg: String) extends RuntimeException(msg) with NoStackTrace
