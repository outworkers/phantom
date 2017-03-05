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
package com.outworkers.phantom.jdk8.tables

import com.outworkers.phantom.connectors.CassandraConnection
import com.outworkers.phantom.database.Database
import com.outworkers.phantom.tables.Connector

class Jdk8Database(override val connector: CassandraConnection) extends Database[Jdk8Database](connector) {
  object primitivesJdk8 extends PrimitivesJdk8 with Connector
  object optionalPrimitivesJdk8 extends OptionalPrimitivesJdk8 with Connector
}

object Jdk8Database extends Jdk8Database(Connector.default)