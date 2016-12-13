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
package com.outworkers.phantom.example.basics

import com.outworkers.phantom.connectors.{ContactPoint, ContactPoints}
import com.outworkers.phantom.connectors._

object Defaults {

  val connector = ContactPoint.local.noHeartbeat().keySpace("phantom_example")
}

/**
 * This is an example of how to connect to Cassandra in the easiest possible way.
 * The SimpleCassandraConnector is designed to get you up and running immediately, with almost 0 effort.
 *
 * What you have to do now is to tell phantom what keyspace you will be using in Cassandra. This connector will automaticalyl try to connect to localhost:9042.
 * If you want to tell the connector to use a different host:port combination, simply override the address inside it.
 *
 * Otherwise, simply mixing this connector in will magically inject a database session for all your queries and you can immediately run them.
 */
trait ExampleConnector extends Defaults.connector.Connector

/**
 * This is an example of how to connect to a custom set of hosts and ports.
 * First, we need to obtain a connector and keep a singleton reference to it.
 * It's really important to guarantee we are using a singleton here, otherwise
 * we will end up spawning a cluster on every call.
 */
object RemoteConnector {

  // Simply specify the list of hosts followed by the keyspace.
  // Now the connector object will automatically create the Database connection for us and initialise it.
  val connector = ContactPoints(Seq("docker.local")).keySpace("phantom_example")
}

trait DockerConnector extends RemoteConnector.connector.Connector