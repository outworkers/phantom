phantom
[![Build Status](https://travis-ci.org/outworkers/phantom.svg?branch=develop)](https://travis-ci.org/outworkers/phantom?branch=develop) [![Coverage Status](https://coveralls.io/repos/github/outworkers/phantom/badge.svg?branch=develop)](https://coveralls.io/github/outworkers/phantom?branch=develop)  [![Codacy Rating](https://api.codacy.com/project/badge/grade/25bee222a7d142ff8151e6ceb39151b4)](https://www.codacy.com/app/flavian/phantom_2) [![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.outworkers/phantom-dsl_2.11/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.outworkers/phantom-dsl_2.11) [![Bintray](https://api.bintray.com/packages/outworkers/oss-releases/phantom-dsl/images/download.svg) ](https://bintray.com/outworkers/oss-releases/phantom-dsl/_latestVersion) [![ScalaDoc](http://javadoc-badge.appspot.com/com.outworkers/phantom-dsl_2.11.svg?label=scaladoc)](http://javadoc-badge.appspot.com/com.outworkers/phantom-dsl_2.11) [![Gitter](https://badges.gitter.im/Join%20Chat.svg)](https://gitter.im/outworkers/phantom?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)
===============================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================
<a id="batch-statements">Batch statements</a>
=============================================

Phantom also brings in support for batch statements. To use them, see [IterateeBigTest.scala](https://github.com/outworkers/phantom/blob/develop/phantom-dsl/src/test/scala/com/outworkers/phantom/builder/query/db/iteratee/IterateeBigReadPerformanceTest.scala). Before you read further, you should remember **batch statements are not used to improve performance**.

Read [the official docs](http://docs.datastax.com/en/cql/3.1/cql/cql_reference/batch_r.html) for more details, but in short **batches guarantee atomicity and they are about 30% slower on average than parallel writes** at least, as they require more round trips. If you think you're optimising performance with batches, you might need to find alternative means.

We have tested with 100 statements per batch, and 1000 batches processed simultaneously. Before you run the test, beware that it takes ~40 minutes.

Batches use lazy iterators and daisy chain them to offer thread safe behaviour. They are not memory intensive and you can expect consistent processing speed even with very large numbers of batches.

Batches are immutable and adding a new record will result in a new Batch, just like most things Scala, so be careful to chain the calls.

phantom also supports `COUNTER` batch updates and `UNLOGGED` batch updates.

To start, we need an example database connection.

```tut:silent

import com.datastax.driver.core.SocketOptions
import com.outworkers.phantom.connectors._
import com.outworkers.phantom.dsl._

object Connector {
  val default: CassandraConnection = ContactPoint.local
    .withClusterBuilder(_.withSocketOptions(
      new SocketOptions()
        .setConnectTimeoutMillis(20000)
        .setReadTimeoutMillis(20000)
      )
    ).noHeartbeat().keySpace(
      KeySpace("phantom").ifNotExists().`with`(
        replication eqs SimpleStrategy.replication_factor(1)
      )
    )
}
```

Now let's define a few tables to allow us to exemplify batch queries.

```tut:silent

import scala.concurrent.Future
import com.outworkers.phantom.dsl._

case class CounterRecord(id: UUID, count: Long)

abstract class CounterTableTest extends Table[
  CounterTableTest,
  CounterRecord
] {
  object id extends UUIDColumn with PartitionKey
  object entries extends CounterColumn
}

case class Article(
  id: UUID,
  name: String,
  content: String
)

abstract class Articles extends Table[Articles, Article] {
  object id extends UUIDColumn with PartitionKey
  object name extends StringColumn
  object content extends StringColumn
}

class TestDatabase(
  override val connector: CassandraConnection
) extends Database[TestDatabase](connector) {
  object articles extends Articles with Connector
  object counterTable extends CounterTableTest with Connector
}

object TestDatabase extends TestDatabase(Connector.default)

trait TestDbProvider extends DatabaseProvider[TestDatabase] {
  override val database = TestDatabase
}

```

<a id="logged-batch-statements">LOGGED batch statements</a>
===========================================================

```tut:silent

import java.util.UUID
import com.outworkers.phantom.dsl._

trait LoggedQueries extends TestDbProvider {

  Batch.logged
    .add(db.articles.update.where(_.id eqs UUID.randomUUID).modify(_.name setTo "blabla"))
    .add(db.articles.update.where(_.id eqs UUID.randomUUID).modify(_.content setTo "blabla2"))
    .future()
}

```


<a id="unlogged-batch-statements">UNLOGGED batch statements</a>
============================================================

```tut:silent

import com.outworkers.phantom.dsl._

trait UnloggedQueries extends TestDbProvider {

  Batch.unlogged
    .add(db.articles.update.where(_.id eqs UUID.randomUUID).modify(_.name setTo "blabla"))
    .add(db.articles.update.where(_.id eqs UUID.randomUUID).modify(_.content setTo "blabla2"))
    .future()
}

```


<a id="counter-batch-statements">COUNTER batch statements</a>
============================================================
<a href="#table-of-contents">back to top</a>

```tut:silent

import com.outworkers.phantom.dsl._

trait CounterQueries extends TestDbProvider {

  Batch.counter
    .add(db.counterTable.update.where(_.id eqs UUID.randomUUID).modify(_.entries increment 500L))
    .add(db.counterTable.update.where(_.id eqs UUID.randomUUID).modify(_.entries decrement 300L))
    .future()
}

```

Counter operations also offer a standard overloaded operator syntax, so instead of `increment` and `decrement`
you can also use `+=` and `-=` to achieve the same thing.

```tut:silent

import com.outworkers.phantom.dsl._

trait CounterOpsQueries extends TestDbProvider {

  Batch.counter
    .add(db.counterTable.update.where(_.id eqs UUID.randomUUID).modify(_.entries += 500L))
    .add(db.counterTable.update.where(_.id eqs UUID.randomUUID).modify(_.entries -= 300L))
    .future()
}    
```

