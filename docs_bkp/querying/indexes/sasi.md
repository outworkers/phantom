### SASI Index support

Available as of phantom 2.11.0, SASI indexes introduce support for a Cassandra 3.4+ feature, namely SS Table attached
secondary indexes. For more details on the internals of SASI within Cassandra, the details are [here](http://www.doanduyhai.com/blog/?p=2058)
or [here](http://batey.info/cassandra-sasi.html).

SASI was an attempt to improve performance on the more traditional secondary indexing, which is notoriously unreliabl
performance wise after a couple thousand records.


### Using SASI support in phantom.

SASI indexes are natively supported in the standard `phantom-dsl` module, so as long as you have the following in your 
`build.sbt` you will not require any special dependencies.

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.outworkers/phantom-dsl_2.11/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.outworkers/phantom-dsl_2.11)

```scala

val phantomVersion = "__check_badge_above__"

libraryDependencies ++= Seq(
  "com.outworkers" %% "phantom-dsl" % phantomVersion
)
```

Then let's start from a simple Cassandra connection:

```scala

import com.datastax.driver.core.SocketOptions
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

case class MultiSASIRecord(
  id: UUID,
  name: String,
  customers: Int,
  phoneNumber: String,
  set: Set[Int],
  list: List[String]
)

abstract class MultiSASITable extends Table[MultiSASITable, MultiSASIRecord] {
  object id extends UUIDColumn with PartitionKey

  object name extends StringColumn with SASIIndex[Mode.Contains] {
    override def analyzer: NonTokenizingAnalyzer[Mode.Contains] = {
      Analyzer.NonTokenizingAnalyzer[Mode.Contains]().normalizeLowercase(true)
    }
  }

  object customers extends IntColumn with SASIIndex[Mode.Sparse] {
    override def analyzer: Analyzer[Mode.Sparse] = Analyzer[Mode.Sparse]()
  }

  object phoneNumber extends StringColumn with SASIIndex[Mode.Prefix] {
    override def analyzer: StandardAnalyzer[Mode.Prefix] = {
      Analyzer.StandardAnalyzer[Mode.Prefix]().skipStopWords(true).enableStemming(true)
    }
  }

  object setCol extends SetColumn[Int]
  object listCol extends ListColumn[String]
}


class SASIDatabase(override val connector: CassandraConnection) extends Database[SASIDatabase](connector) {
  object multiSasiTable extends MultiSASITable with Connector
}

object db extends SASIDatabase(Connector.default)

```


#### Analyzers

SASI ships with 3 basic analyzers that are re-created in phantom.

- `Analzyer.NonTokenizingAnalyzer`
- `Analyzer.StandardAnalyzer`
- `Analyzer.DefaultAnalyzer`

The `DefaultAnalyzer` will allow you to set all the properties from the root, and the other two are nothing more
than specialised forms.


#### Modes

Phantom SASI support incldues all three supported modes for SASI. All analyzers include support for basic comparison
 operations, as listed below. Some operators have two flavours, such as `==` and `eqs`, `<` and `lt` and so on. They
 are all available via the standard `import com.outworkers.phantom.dsl._` import.
 
  
| Operator | Natural Language equivalent            |
| -------- | -------------------------------------- |
| `==`     | Equality operator                      |
| `eqs`    | Equality operator                      |
| `<`      | Strictly lower than operator           |
| `lt`     | Strictly lower than operator           |
| `<=`     | Lower than or equal to operator        |
| `lte`    | Lower than or equal to operator        |
| `>`      | Strictly greater than operator         |
| `gt`     | Strictly greater than operator         |
| `>=`     | Greater than or equal to operator      |
| `gte`    | Greater than or equal to operator      |   
  
  
There are two modes directed specifically at text columns, namely `Mode.Prefix` and `Mode.Contains`. By using
  these modes, you will be able to perform text specific queries using the `like` operator. 
  
##### Mode.Prefix

In addition to the standard operations, the `Prefix` mode will allow you to perform `like(prefix("text"))` style
 queries.
 
Examples can be found in [SASIIntegrationTest.scala](/phantom-dsl/src/test/scala/com/outworkers/phantom/builder/query/sasi/SASIIntegrationTest.scala).
 
Example query, based on the schema defined above.

```scala

import com.outworkers.phantom.dsl._

trait PrefixExamples extends db.Connector {
  db.multiSasiTable.select.where(_.phoneNumber like prefix("example")).fetch()
}
```
  
#### Mode.Contains
  
This will enable further queries for text columns, such as `like(suffix("value"))` and `like(contains("value`))`, as well
as prefix style queries.

Examples can be found in [SASIIntegrationTest.scala](/phantom-dsl/src/test/scala/com/outworkers/phantom/builder/query/sasi/SASIIntegrationTest.scala).

Example possible queries, based on the schema defined above.

```scala

import com.outworkers.phantom.dsl._

val pre = "text"

trait ContainsExamples extends db.Connector {
  db.multiSasiTable.select.where(_.name like prefix(pre)).fetch()
  db.multiSasiTable.select.where(_.name like contains(pre)).fetch()
  db.multiSasiTable.select.where(_.name like suffix(pre)).fetch()
}


```



#### Mode.Sparse

As suggested in the official SASI tutorial, `Mode.Sparse` is directly targeted at numerical columns and it's a way
to enable standard operators for numerical columns that are not part of the primary key. All standard operators can be used.

Sparse mode SASI indexes cannot define analyzers, and automated schema creation will fail if you attempt to use an analyzer
in `Mode.Sparse`

Examples can be found in [SASIIntegrationTest.scala](/phantom-dsl/src/test/scala/com/outworkers/phantom/builder/query/sasi/SASIIntegrationTest.scala).

Example possible queries.

```scala
import com.outworkers.phantom.dsl._

trait SparseExamples extends db.Connector {
  db.multiSasiTable.select.where(_.customers eqs 50).fetch()

  // Select all entries with at least 50 customers
  db.multiSasiTable.select.where(_.customers >= 50).fetch()

  // Select all entries with at most 50 customers
  db.multiSasiTable.select.where(_.customers <= 50).fetch()
}
```
