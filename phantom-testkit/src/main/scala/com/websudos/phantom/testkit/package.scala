package com.websudos.phantom

package object testkit {
  type PhantomCassandraTestSuite = com.websudos.phantom.testkit.suites.PhantomCassandraTestSuite
  type CassandraFlatSpec = com.websudos.phantom.testkit.suites.CassandraFlatSpec
  type CassandraFeatureSpec = com.websudos.phantom.testkit.suites.CassandraFeatureSpec
  type SimpleCassandraTest = com.websudos.phantom.testkit.suites.SimpleCassandraTest
}
