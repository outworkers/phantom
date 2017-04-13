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
package com.outworkers.phantom.streams.suites.iteratee

import com.datastax.driver.core.{Session, SocketOptions}
import com.outworkers.phantom.PhantomSuite
import com.outworkers.phantom.connectors.ContactPoint

trait BigTest extends PhantomSuite {

  val connectionTimeoutMillis = 1000

  override implicit lazy val session: Session = {
    ContactPoint.local.withClusterBuilder(
      _.withSocketOptions(new SocketOptions()
        .setReadTimeoutMillis(connectionTimeoutMillis)
        .setConnectTimeoutMillis(connectionTimeoutMillis)
      )
    ).noHeartbeat().keySpace(space.name).session
  }
}
