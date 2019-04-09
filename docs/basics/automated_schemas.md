| CI  | Test coverage(%) | Code quality | Stable version | ScalaDoc | Chat | Open issues | Average issue resolution time | 
| --- | ---------------- | -------------| -------------- | -------- | ---- | ----------- | ----------------------------- |
| [![Build Status](https://travis-ci.org/outworkers/phantom.svg?branch=develop)](https://travis-ci.org/outworkers/phantom?branch=develop) | [![Coverage Status](https://coveralls.io/repos/github/outworkers/phantom/badge.svg?branch=develop)](https://coveralls.io/github/outworkers/phantom?branch=develop) | [![Codacy Rating](https://api.codacy.com/project/badge/grade/25bee222a7d142ff8151e6ceb39151b4)](https://www.codacy.com/app/flavian/phantom_2) | [![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.outworkers/phantom-dsl_2.11/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.outworkers/phantom-dsl_2.11) | [![ScalaDoc](http://javadoc-badge.appspot.com/com.outworkers/phantom-dsl_2.11.svg?label=scaladoc)](http://javadoc-badge.appspot.com/com.outworkers/phantom-dsl_2.11) | [![Gitter](https://badges.gitter.im/Join%20Chat.svg)](https://gitter.im/outworkers/phantom?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge) | [![Percentage of issues still open](http://isitmaintained.com/badge/open/outworkers/phantom.svg)](http://isitmaintained.com/project/outworkers/phantom "%% of issues still open") | [![Average time to resolve an issue](http://isitmaintained.com/badge/resolution/outworkers/phantom.svg)](http://isitmaintained.com/project/outworkers/phantom "Average time to resolve an issue") |


<a id="automated-schema-generation">Automated schema generation</a>
===================================================================

One of the most convenient features of phantom is that you can drive your schema directly from the code. So instead of
having to create the schema first inside Cassandra and then struggle to write matching code, you can drive your entire
database layer from the Scala code.

This is called schema auto-generation, and it's pretty much self explanatory. Phantom will provide you with simple methods
to allow you to drive the schema from the code. Let's explore this simple schema and the database below.

```scala

import scala.concurrent.Future
import com.outworkers.phantom.dsl._

case class User(id: UUID, email: String, name: String)

abstract class Users extends Table[Users, User] {
  object id extends UUIDColumn with PartitionKey
  object email extends StringColumn
  object name extends StringColumn

  def findById(id: UUID): Future[Option[User]] = {
    select.where(_.id eqs id).one()
  }
}

abstract class UsersByEmail extends Table[UsersByEmail, User] {
  object email extends StringColumn with PartitionKey
  object id extends UUIDColumn
  object name extends StringColumn

  def findByEmail(email: String): Future[Option[User]] = {
    select.where(_.email eqs email).one()
  }
}

class AppDatabase(
  override val connector: CassandraConnection
) extends Database[AppDatabase](connector) {
  object users extends Users with Connector
  object usersByEmail extends UsersByEmail with Connector
}
```

The simplest level of auto-generation is at table level. Let's look at how we could create the schema automatically.

```scala

object TestConnector {
  val connector = ContactPoint.local
    .noHeartbeat()
    .keySpace("myapp_example")
}

object TestDatabase extends AppDatabase(TestConnector.connector)

```


More advanced indexing scenarios
================================

Secondary indexes are a non scalable flavour of Cassandra indexing that allows us to query certain columns without needing to duplicate data. They do not scale very well at well, but they remain useful for tables where the predicted cardinality for such records is very small.

That aside, it's worth noting phantom is capable of auto-generating your CQL schema and initialising all your indexes automatically, and this functionality is exposed through the exact same `table.create.future()`.

```scala


import com.outworkers.phantom.dsl._

case class TestRow(
  key: String,
  list: List[String],
  setText: Set[String],
  mapTextToText: Map[String, String],
  setInt: Set[Int],
  mapIntToText: Map[Int, String],
  mapIntToInt: Map[Int, Int]
)

abstract class IndexedCollectionsTable extends Table[IndexedCollectionsTable, TestRow] {

  object key extends StringColumn with PartitionKey

  object list extends ListColumn[String]

  object setText extends SetColumn[String] with Index

  object mapTextToText extends MapColumn[String, String] with Index

  object setInt extends SetColumn[Int]

  object mapIntToText extends MapColumn[Int, String] with Index with Keys

  object mapIntToInt extends MapColumn[Int, Int] with Index with Entries
}
