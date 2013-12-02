package com.newzly.phantom.dsl.test

import org.specs2._
import org.specs2.specification.{ Step, Fragments }
import com.datastax.driver.core.Cluster
import com.newzly.cassandra.phantom.CassandraCluster;

trait CassandraSpec extends mutable.Specification with CassandraCluster {

  def start = {
    scala.util.Try(session.execute("DROP KEYSPACE newzly;"))
    session.execute("CREATE KEYSPACE newzly WITH replication = {'class': 'SimpleStrategy', 'replication_factor' : 1};")
    session.execute("use blackpepper;")
    val recipesTable =
      """|CREATE TABLE recipes (
        |url text PRIMARY KEY,
        |description text,
        |ingredients list<text>,
        |author text,
        |servings int,
        |tags set<text>,
        |last_checked_at timestamp,
        |props map<text, text>,
        |uid timeuuid);
      """.stripMargin
    session.execute(recipesTable)
  }

  override def map(fs: => Fragments) = Step(start) ^ fs ^ Step(cluster.shutdown())
}