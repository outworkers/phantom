package com.websudos.phantom.example

import com.websudos.phantom.Manager._
import com.websudos.phantom.example.advanced.RecipesDatabase
import com.websudos.phantom.testkit._

import scala.concurrent.Await
import scala.concurrent.duration._

class ExampleSuite extends CassandraFlatSpec with RecipesDatabase.connector.Connector {

  override def beforeAll(): Unit = {
    super.beforeAll()
    Await.ready(RecipesDatabase.autocreate().future(), 5.seconds)
  }

  override def afterAll(): Unit = {
    super.afterAll()
    Await.ready(RecipesDatabase.autotruncate().future(), 8.seconds)
  }
}


