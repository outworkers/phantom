package com.websudos.phantom

package object testkit {
  type PhantomCassandraTestSuite = com.websudos.phantom.testkit.suites.PhantomCassandraTestSuite
  type PhantomCassandraConnector = com.websudos.phantom.testkit.suites.PhantomCassandraConnector
  type CassandraFlatSpec = com.websudos.phantom.testkit.suites.CassandraFlatSpec
  type CassandraFeatureSpec = com.websudos.phantom.testkit.suites.CassandraFeatureSpec
  type SimpleCassandraTest = com.websudos.phantom.testkit.suites.SimpleCassandraTest
  type TestZookeeperConnector = com.websudos.phantom.testkit.suites.TestZookeeperConnector
}
