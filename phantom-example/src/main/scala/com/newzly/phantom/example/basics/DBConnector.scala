/*
 * Copyright 2013 newzly ltd.
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
package com.newzly.phantom.example.basics

import scala.concurrent. { blocking, Future }
import com.datastax.driver.core.{ Cluster, Session }
import com.newzly.phantom.Implicits._

object DBConnector {
  val keySpace = "phantom_examples"

  lazy val cluster =  Cluster.builder()
    .addContactPoint("localhost")
    .withPort(9042)
    .withoutJMXReporting()
    .withoutMetrics()
    .build()

  lazy val session = blocking {
    cluster.connect(keySpace)
  }
}

trait DBConnector {
  self: CassandraTable[_, _] =>

  def createTable(): Future[Unit] ={
    create.future() map (_ => ())
  }

  implicit lazy val datastax: Session = DBConnector.session
}