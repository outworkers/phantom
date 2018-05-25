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
package com.outworkers.phantom.readme


import com.datastax.driver.core.{PlainTextAuthProvider, SocketOptions}
import com.outworkers.phantom.dsl._

object ConnectorExample2 {

  val default: CassandraConnection = ContactPoint.local
    .withClusterBuilder(
      _.withSocketOptions(
        new SocketOptions()
          .setConnectTimeoutMillis(20000)
          .setReadTimeoutMillis(20000)
      ).withAuthProvider(
        new PlainTextAuthProvider("username", "password")
      )
  ).keySpace(
    KeySpace("phantom").ifNotExists().`with`(
      replication eqs SimpleStrategy.replication_factor(1)
    )
  )
}