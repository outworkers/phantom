package com.websudos.phantom.connectors

import org.scalatest.{ FlatSpec, Matchers }


trait MyConnector extends SimpleCassandraConnector {
  implicit val keySpace = KeySpace("test")
}

object Test extends MyConnector

object Test2 extends MyConnector


class ConnectorReferenceTest extends FlatSpec with Matchers {
  it should "reference the same session inside multiple mixins of the same connector" in {
    (Test.session eq Test2.session) shouldEqual true
  }

  it should "reference the same manager inside multiple mixins of the same connctor" in {
    (Test.manager eq Test2.manager) shouldEqual true
  }

  it should "reference the same cluster inside multiple mixins of the same connctor" in {
    (Test.manager.clusterRef eq Test2.manager.clusterRef) shouldEqual true
  }
}
