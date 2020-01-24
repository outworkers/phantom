package com.outworkers.phantom.connectors

import com.datastax.driver.core.VersionNumber
import org.scalamock.scalatest.MockFactory
import org.scalatest.{Matchers, OptionValues, WordSpec}

class CassandraConnectionTest extends WordSpec with MockFactory with Matchers with OptionValues {
  "cassandraVersionOpt" should {
    "return the cassandra version when there is only one cassandra version" in {
      val connection = new TestCassandraConnection(Set(VersionNumber.parse("3.0.0"), VersionNumber.parse("3.0.0")))
      connection.cassandraVersionOpt.value shouldEqual VersionNumber.parse("3.0.0")
    }
    "return no Cassandra version when there is no cassandra version" in {
      val connection = new TestCassandraConnection(Set.empty)
      connection.cassandraVersionOpt shouldBe empty
    }
    "return no Cassandra version when there are multiple cassandra versions" in {
      val connection = new TestCassandraConnection(Set(VersionNumber.parse("3.0.0"), VersionNumber.parse("3.1.0")))
      connection.cassandraVersionOpt shouldBe empty
    }
  }
  "cassandraVersion" should {
    "return the cassandra version when there is only one cassandra version" in {
      val connection = new TestCassandraConnection(Set(VersionNumber.parse("3.0.0"), VersionNumber.parse("3.0.0")))
      connection.cassandraVersion.value shouldEqual VersionNumber.parse("3.0.0")
    }
    "throw an exception when there is no cassandra version" in {
      val connection = new TestCassandraConnection(Set.empty)
      val thrown = the[RuntimeException] thrownBy connection.cassandraVersion
      thrown.getMessage shouldEqual "Could not extract any versions from the cluster, versions were empty"
    }
    "return no Cassandra version when there are multiple cassandra versions" in {
      val connection = new TestCassandraConnection(Set(VersionNumber.parse("3.0.0"), VersionNumber.parse("3.1.0")))
      val thrown = the[RuntimeException] thrownBy connection.cassandraVersion
      thrown.getMessage shouldEqual "Illegal single version comparison. You are connected to clusters of different versions.Available versions are: 3.0.0, 3.1.0"
    }
  }

  class TestCassandraConnection(cassandraVersionsToUse: Set[VersionNumber]) extends CassandraConnection("name", mock[ClusterBuilder], false) {
    override def cassandraVersions: Set[VersionNumber] = cassandraVersionsToUse
  }
}
