package com.websudos.phantom.example

import scala.concurrent.Await
import scala.concurrent.duration._

import com.websudos.phantom.Manager
import com.websudos.phantom.Manager._
import com.websudos.phantom.connectors.KeySpace
import com.websudos.phantom.testkit._

sealed trait KeySpaceDefinition {
  implicit val keySpace = KeySpace("phantom_example")
}

class ExampleFlatSuite extends CassandraFlatSpec with KeySpaceDefinition {

  override def beforeAll(): Unit = {
    super.beforeAll()
    Await.ready(Manager.autocreate().future(), 5.seconds)
  }

  override def afterAll(): Unit = {
    super.afterAll()
    Await.ready(Manager.autotruncate().future(), 8.seconds)
  }
}


