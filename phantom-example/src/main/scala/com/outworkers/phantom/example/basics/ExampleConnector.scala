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
 * - Explicit consent must be obtained from the copyright owner, Outworkers Limited before any redistribution is made.
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
package com.outworkers.phantom.example.basics

import com.outworkers.phantom.connectors.{ContactPoint, ContactPoints}
import com.outworkers.phantom.connectors._

object Defaults {

  val connector = ContactPoint.local.keySpace("phantom_example")
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