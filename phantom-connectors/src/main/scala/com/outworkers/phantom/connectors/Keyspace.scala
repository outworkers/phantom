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
package com.outworkers.phantom.connectors

import com.datastax.driver.core.{ProtocolVersion, Session}

trait SessionAugmenter {

  def session: Session

  def protocolVersion: ProtocolVersion = {
    session.getCluster.getConfiguration.getProtocolOptions.getProtocolVersion
  }

  def isNewerThan(pv: ProtocolVersion): Boolean = {
    protocolVersion.compareTo(pv) > 0
  }

  def v3orNewer: Boolean = isNewerThan(ProtocolVersion.V2)

  def protocolConsistency: Boolean = isNewerThan(ProtocolVersion.V1)

  def v4orNewer: Boolean = isNewerThan(ProtocolVersion.V3)
}

trait SessionAugmenterImplicits {
  implicit class RichSession(val session: Session) extends SessionAugmenter
}

case class KeySpace(name: String)
