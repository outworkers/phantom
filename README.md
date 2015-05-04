phantom [![Build Status](https://travis-ci.org/websudos/phantom.svg?branch=develop)](https://travis-ci.org/websudos/phantom) [![Coverage Status](https://coveralls.io/repos/websudos/phantom/badge.svg)](https://coveralls.io/r/websudos/phantom) [![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.websudos/phantom_2.10/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.websudos/phantom_2.10)

==============
Reactive type-safe Scala DSL for Cassandra

To stay up-to-date with our latest releases and news, follow us on Twitter: [@websudos](https://twitter.com/websudos).

If you use phantom, please consider adding your company to our list of adopters. Phantom is and will always be completely free and open source, 
but the more adopters our projects have, the more people from our company will actively work to make them better.


![phantom](https://s3-eu-west-1.amazonaws.com/websudos/oss/logos/phantom.png "Websudos Phantom")


Using phantom
=============

### Scala 2.10 and 2.11 releases ###

We publish phantom in 2 formats, stable releases and bleeding edge.

- The stable release is always available on Maven Central and will have a version with a patch number "0". E.g "x.x.0".

- Intermediary releases are available through our managed Maven repository,```"Websudos releases" at "http://maven.websudos.co.uk/ext-release-local"```.


### Latest versions

- Latest stable version: 1.8.0 (Maven Central)
- Bleeding edge: 1.8.0 (Websudos Maven Repo)

You will also be needing the default resolvers for Maven Central and the typesafe releases. Phantom will never rely on any snapshots or be published as a
snapshot version, the bleeding edge is always subject to internal scrutiny before any releases into the wild.

The Apache Cassandra version used for auto-embedding Cassandra during tests is: ```val cassandraVersion = "2.1.0-rc5"```. You will require JDK 7 to use 
Cassandra, otherwise you will get an error when phantom tries to start the embedded database. The recommended JDK is the Oracle variant.


### Version highlights and upcoming features ###

<ul>
    <li><a href="#new-querybuilder">1.8.0: A new QueryBuilder, written from the ground up, in idiomatic Scala</a></li>
    <li><a href="#alter-queries">1.8.0: Added support for type-safe ALTER queries</a></li>
    <li><a href="#advanced-cql-support">1.8.0: Support for advanced CQL options</a></li> 
    <li><a href="#prepared-statements">1.9.0: Type safe prepared statements</a></li>
    <li><a href="#automigration">1.9.0: Automated Schema migrations</li>
    <li><a href="#udts">2.0.0: Type safe user defined types</li>
    <li>
      <a href="#breaking-changes">Breaking changes in DSL and connectors
      <ul>
        <li><a href="#new-imports">A new import structure</a></li>
        <li><a href="#propagating-parse-errors">Propagating parse errors</a></li>
      </ul>
    </a>
    <li><a href="#autocreation">1.9.0: Automated table creations</li>
    <li><a href="#autotruncation">1.9.0: Automated table truncation.</li>
    <li><a href="#performance">1.9.0: Big performance improvements</li>
</ul>


### Breaking API changes in Phantom 1.8.0 and beyond.

The 1.8.0 release constitutes a major re-working of a wide number of internal phantom primitives, including but not limited to a brand new Scala flavoured
QueryBuilder with full support for all CQL 3 features and even some of the more "esoteric" options available in CQL. We went above and beyond to try and
offer a tool that's comprehensive and doesn't miss out on any feature of the protocol, no matter how small.

If you are wondering what happened to 1.7.0, it was never publicly released as testing the new querybuilder entailed serious internal efforts and for such a drastic change
we wanted to do as much as possible to eliminate books. Surely there will be some still found, but hopefully very few and with your help they will be very short lived.

Ditching the Java Driver was not a question of code quality in the driver, but rather an opportunity to exploit the more advanced Scala type system features
to introduce behaviour such as preventing duplicate limits on queries using phantom types, to prevent even more invalid queries from compiling, and to switch
 to a fully immutable QueryBuilder that's more in tone with idiomatic Scala, as opposed to the Java-esque mutable alternative already existing the java driver.


<a id="new-imports">A new import structure</a>
================================================

```import com.websudos.phantom.Implicits._``` has now been renamed to ```import com.websudos.phantom.dsl._```. The old import is still there but deprecated.

A natural question you may ask is why we resorted to seemingly unimportant changes, but the goal here was to enforce the new implicit mechanism and use a uniform importing experience across all modules.
So you can have the series of ```import com.websudos.phantom.dsl._, import com.websudos.phantom.thrift._, import com.websudos.phantom.testkit._``` and so on, all identical, all using Scala ```package object``` definitions as intended.

<a id="propagating-parse-errors">Propagating parse errors</a>
=============================================================

Until now, our implementation of Cassandra primitives has been based on the Datastax Java Driver and on an ```Option``` based DSL. This made it heard to deal with parse errors at runtime, specifically those situations when
the DSL was unable to parse the required type from the Cassandra result or in a simple case where ```null`` was returned for a non-optional column.

The core of the ```Column[Table, Record, ValueType].apply(value: ValueType]``` method which was used to parse rows in a type safe manner was written like this:

```scala

import com.datastax.driver.core.Row

def apply(row: Row):  = optional(row).getOrElse(throw new Exception("Couldn't parse things")

```

This approach left the original exception which caused the parser to parse a ```null``` and subsequently a ```None``` was ignored.

With the new type-safe primitive interface that no longer relies on the Datastax Java driver we were also able to move the ```Option``` based parsing mechanism to a ```Try``` mechanism which will now
 log all parse errors un-altered, in the exact same way the are thrown at compile time, using the ```logger``` for the given table.
 
Internally, we are now using something like this: 
 
```scala

   def optional(r: Row): Try[T]
 
   def apply(r: Row): T = optional(r) match {
     case Success(value) => value
     case Failure(ex) => {
       table.logger.error(ex.getMessage)
       throw ex
     }
   }

```

The exception is now logged and propagated with no interference. We intercept it to provide consistent logging in the same table logger where you would naturally monitor for logs. 


<a id="improving-query-performance">Improving query performance</a>
==================================================================

Play enumerators and Twitter ResultSpools have been removed from the default ```one```, ```get```, ```fetch``` and ```collect``` methods. You will have to
explicitly call ```fetchEnumerator``` and ```fetchSpool``` if you want result throttling through async lazy iterators. This will offer everyone a signifact
performance improvement over query performance. Async iterators needed a lot of expensive "magic" to work properly, but you don't always need to fold over
100k records. That behaviour was implemented both as means of showing off as well as doing all in one loads like the Spark - Cassandra connector performs. E.g
 dumping C* data into HDFS or whatever backup system. A big 60 - 70% gain should be expected.

Phantom connectors now require an ```implicit com.websudos.phantom.connectors.KeySpace``` to be defined. Instead of using a plain string, you just have to
use ```KeySpace.apply``` or simply: ```trait MyConnector extends Connector { implicit val keySpace = KeySpace("your_def") } ```. This change allows us to
replace the existing connector model and vastly improve the number of concurrent cluster connections required to perform operations on various keyspaces.
Insteaed of the 1 per keyspace model, we can now successfully re-use the same session without evening needing to switch as phantom will use the full CQL
reference syntax, e.g ```SELECT FROM keyspace.table``` instead of ```SELECY FROM table```.

A entirely new set of options have been enabled in the type safe DSLs. You can now alter tables, specify advanced compressor behaviour and so forth, all
from within phantom and with the guarantee of auto-completion and type safety.


#### Support for ALTER queries.

This was never possible before in phantom, and now from 1.7.0 onwards we feature full support for using ALTER queries.



<a id="table-of-contents">Table of contents</a>
===============================================

<ul>
  <li><a href="#issues-and-questions">Issues and questions</a></li>
  <li><a href="#adopters">Adopters</a></li>
  <li><a href="#roadmap">Roadmap</a></li>
  <li><a href="#learning-phantom">Tutorials on phantom and Cassandra</a></li>
  <li><a href="#commercial-support">Commercial support</a></li>
  <li><a href="#integrating-phantom-in-your-project">Using phantom in your project</a></li>

<li>
  <p>Phantom columns</p>
  <ul>
    <li><a href="#primitive-columns">Primitive columns</a></li>
    <li><a href="#optional-primitive-columns">Optional primitive columns</a></li>
    <li><a href="#collection-columns">Collection columns</a></li>
    <li><a href="#collections-and-operators">Collection operators</a></li>
    <li><a href="#list-operators">List operators</a></li>
    <li><a href="#set-operators">Set operators</a></li>
    <li><a href="#map-operators">Map operators</a></li>
    <li><a href="#automated-schema-generation">Automated schema generation</a></li>
    <li>
      <p><a href="#indexing-columns">Indexing columns</a></p>
      <ul>
        <li><a href="#partition-key">PartitionKey</a></li>
        <li><a href="#primary-key">PrimaryKey</a></li>
        <li><a href="#secondary-index">SecondaryIndex</a></li>
        <li><a href="#clustering-order">ClusteringOrder</a></li>
      </ul>
    </li>
    <li><a href="#thrift-columns">Thrift columns</a></li>
  </ul>
</li>
<li><a href="#data-modeling-with-phantom">Data modeling with phantom</a></li>
<li>
  <p><a href="#querying-with-phantom">Querying with phantom</a></p>
  <ul>
    <li><a href="#common-query-methods">Common query methods</a></li>
    <li><a href="#select-queries">SELECT queries</a></li>
    <li><a href="#partial-select-queries">Partial SELECT queries</a></li>
    <li><a href="#where-and-operators">WHERE and AND clause operators</a></li>
    <li><a href="#create-queries">CREATE queries</a></li>
    <li><a href="#insert-queries">INSERT queries</a></li>
    <li><a href="#update-queries">UPDATE queries</a></li>
    <li><a href="#delete-queries">DELETE queries</a></li>
    <li><a href="#alter-queries">ALTER queries</a></li>
    <li><a href="#truncate-queries">TRUNCATE queries</a></li>
  </ul>
  
  <ul>
    <p>Basic query examples</p>
    <li><a href="#query-api">Query API</a></li>
    <li><a href="#scala-futures">Using Scala Futures to query</a></li>
    <li><a href="#scala-futures-examples">Examples with Scala Futures</a></li>
    <li><a href="#twitter-futures">Using Twitter Futures to query</a></li>
    <li><a href="#twitter-futures-examples">Examples with Twitter Futures</a></li>
  </ul>
  
<li>

  <ul>
    <p>Cassandra indexing</p>
    <li><a href="#partition-tokens">Using partition tokens</a></li>
    <li><a href="#partition-token-operators">Partition token operators</a></li>
    <li><a href="#compound-keys">Compound Keys</a></li>
    <li><a href="#composite-keys">Composite Keys</a></li>
    <li><a href="#time-series">Cassandra Time Series and ClusteringOrder</a></li>
    <li><a href="#secondary-keys">Secondary Keys</a></li>
  </ul>

<li><a href="#async-iterators">Asynchronous iterators</a></li>
<li>
  <p>Batch statements</p>
  <ul>
    <li><a href="#logged-batch-statements">LOGGED Batch statements</a></li>
    <li><a href="#counter-batch-statements">COUNTER Batch statements</a></li>
    <li><a href="logged-batch-statements">UNLOGGED Batch statements</a></li>
  </ul>
</li>


<li><a href="#thrift-integration">Thrift integration</a></li>

<li>
  <p><a href="apache-zookeeper-integration">Apache ZooKeeper integration</a></p>
  <ul>
    <li><a href="#zookeeper-connectors">ZooKeeper connectors</a></li>
    <li><a href="#the-simple-cassandra-connector">The simple Cassandra connector</a></li>
    <li><a href="#the-default-zookeeper-connector-and-default-zookeeper-manager">The DefaultZooKeeperConnector and DefaultZooKeeperManager</a></li>
    <li><a href="#using-a-zookeeper-instance">Using a ```com.websudos.util.zookeeper.ZooKeeperInstance```</a></li>
  </ul>
</li>

<li>
  <p><a href="#testing-utilities">The phantom testkit</a></p>
  <ul>
    <li><a href="#auto-embedded-cassandra">Auto-embeddeded Cassandra</a></li>
    <li><a href="#using-the-default-suite">Using the default PhantomCassandraSuite to write tests</a></li>
    <li><a href="#using-the-automated-tools">Using the phantom manager to auto-create, auto-migrate and auto-truncate tables</a></li>
    <li>
      <p><a href="#running-tests">Running the tests locally</a></p>
      <ul>
        <li><a href="#scalatest-support">ScalaTest support</a></li>
        <li><a href="#specs2-support">Specs2 support</a></li>
      </ul>
  </ul>
</li>

<li><a href="#contributors">Contributing to phantom</a></li>
<li><a href="#using-gitflow">Using GitFlow as a branching model</a></li>
<li><a href="#scala-style-guidelines">Scala style guidelines for contributions</a></li>
<li><a href="#copyright">Copyright</a></li>


<a id="issues-and-questions">Issues and questions</a>
=====================================================
<a href="#table-of-contents">back to top</a>

We love Cassandra to bits and use it in every bit our stack. phantom makes it super trivial for Scala users to embrace Cassandra.

Cassandra is highly scalable and it's by far the most powerful database technology available, open source or otherwise.

Phantom is built on top of the [Datastax Java Driver](https://github.com/datastax/java-driver), which does most of the heavy lifting. 

If you're completely new to Cassandra, a much better place to start is the [Datastax Introduction to Cassandra](http://www.datastax.com/documentation/getting_started/doc/getting_started/gettingStartedIntro_r.html). An even better introduction is available on [our blog]
(http://blog.websudos.com/category/nosql/cassandra/), where we have a full series of introductory posts to Cassandra with phantom.

We are very happy to help implement missing features in phantom, answer questions about phantom, and occasionally help you out with Cassandra questions! Please use GitHub for any issues or bug reports.

Adopters
========

This is a list of companies that have embraced phantom as part of their technology stack and using it in production environments.

- [CreditSuisse](https://www.credit-suisse.com/global/en/)
- [Pellucid Analytics](http://www.pellucid.com/)
- [Sphonic](http://www.sphonic.com/)
- [websudos](https://www.websudos.com/)
- [Equens](http://www.equens.com/)
- [VictorOps](http://www.victorops.com/)
- [Socrata](http://www.socrata.com)

Roadmap
========

While dates are not fixed, we will use this list to tell you about our plans for the future. If you have great ideas about what could benefit all phantom 
adopters, please get in touch. We are very happy and eager to listen.

- User defined types

We are working closely around the latest features in the Datastax Java driver and Apache Cassandra 2.1 to offer a fully type safe DSL for user defined types.
This feature is well in progress and you can expect to see it live roughly at the same time as the release of the Datastax 2.1 driver, planned for July 2014.

Some of the cool features include automatic schema generation, fully type safe referencing of fields and inner members of UDTs and fully type safe querying.


- Spark integration

Thanks to the recent partnership between Databricks and Datastax, Spark is getting a Cassandra facelift with a Datastax backed integration. We won't be slow to
follow up with a type safe Scala variant of that integration, so you can enjoy the benefits of high power computation with Cassandra as a backup
 storage through the simple high power DSL we've gotten you used to.

- Prepared statements

By popular demand, a feature long overdue in phantom. The main reason is the underlying Java driver and the increased difficulty of guaranteeing type safety
with prepared statements along with a nice DSL to get things done. Not to say it's impossible, this will be released after the new query builder emerges.

- A new QueryBuilder(available as of 1.6.0)

- Zookeeper support(available as of 1.1.0).


<a id="learning-phantom">Tutorials on phantom and Cassandra</a>
======================================================================

Commercial support
===================
<a href="#table-of-contents">back to top</a>

We, the people behind phantom run a software development house specialised in Scala and NoSQL. If you are after enterprise grade
training or support for using phantom, [Websudos](http://websudos.com) is here to help!

We offer a comprehensive range of elite Scala development services, including but not limited to:

- Software development
- Remote contractors for hire
- Advanced Scala and Cassandra training


We are big fans of open source and we will open source every project we can! To read more about our OSS efforts, 
click [here](http://www.websudos.co.uk/work).


<a id="integrating-phantom">Integrating phantom in your project</a>
===================================================================
<a href="#table-of-contents">back to top</a>

The resolvers needed for Phantom are the Typesafe defaults, Sonatype, Twitter and our very own. The below list should make sure you have no dependency
resolution errors.

```scala
resolvers ++= Seq(
  "Typesafe repository snapshots" at "http://repo.typesafe.com/typesafe/snapshots/",
  "Typesafe repository releases" at "http://repo.typesafe.com/typesafe/releases/",
  "Sonatype repo"                    at "https://oss.sonatype.org/content/groups/scala-tools/",
  "Sonatype releases"                at "https://oss.sonatype.org/content/repositories/releases",
  "Sonatype snapshots"               at "https://oss.sonatype.org/content/repositories/snapshots",
  "Sonatype staging"                 at "http://oss.sonatype.org/content/repositories/staging",
  "Java.net Maven2 Repository"       at "http://download.java.net/maven/2/",
  "Twitter Repository"               at "http://maven.twttr.com",
  "Websudos releases"                at "http://maven.websudos.co.uk/ext-release-local"
)
```

For most things, all you need is ```phantom-dsl``` and ```phantom-testkit```. Read through for information on other modules.

```scala
libraryDependencies ++= Seq(
  "com.websudos"  %% "phantom-dsl"                   % phantomVersion,
  "com.websudos"  %% "phantom-testkit"               % phantomVersion
)
```

The full list of available modules is:

```scala
libraryDependencies ++= Seq(
  "com.websudos"  %% "phantom-dsl"                   % phantomVersion,
  "com.websudos"  %% "phantom-example"               % phantomVersion,
  "com.websudos"  %% "phantom-scalatra"              % phantomVersion,
  "com.websudos"  %% "phantom-spark"                 % phantomVersion,
  "com.websudos"  %% "phantom-thrift"                % phantomVersion,
  "com.websudos"  %% "phantom-testkit"               % phantomVersion,
  "com.websudos"  %% "phantom-udt"                   % phantomVersion,
  "com.websudos"  %% "phantom-zookeeper"             % phantomVersion
)
```
If you include ```phantom-zookeeper```, make sure to add the following resolvers:

```scala
resolvers += "twitter-repo" at "http://maven.twttr.com"

resolvers += "websudos-repo" at "http://maven.websudos.co.uk/ext-release-local"
```

<a id="primitive-columns">Primitive columns</a>
====================================================
<a href="#table-of-contents">back to top</a>

This is the list of available columns and how they map to C* data types.
This also includes the newly introduced ```static``` columns in C* 2.0.6.

The type of a static column can be any of the allowed primitive Cassandra types.
phantom won't let you mixin a non-primitive via implicit magic.

| phantom columns               | Java/Scala type           | Cassandra type    |
| ---------------               |-------------------        | ----------------- |
| BlobColumn                    | java.nio.ByteBuffer       | blog              |
| BigDecimalColumn              | scala.math.BigDecimal     | decimal           |
| BigIntColumn                  | scala.math.BigInt         | varint            |
| BooleanColumn                 | scala.Boolean             | boolean           |
| DateColumn                    | java.util.Date            | timestamp         |
| DateTimeColumn                | org.joda.time.DateTime    | timestamp         |
| DoubleColumn                  | scala.Double              | double            |
| EnumColumn                    | scala.Enumeration         | text              |
| FloatColumn                   | scala.Float               | float             |
| IntColumn                     | scala.Int                 | int               |
| InetAddressColumn             | java.net.InetAddress      | inet              |
| LongColumn                    | scala.Long                | long              |
| StringColumn                  | java.lang.String          | text              |
| UUIDColumn                    | java.util.UUID            | uuid              |
| TimeUUIDColumn                | java.util.UUID            | timeuuid          |
| CounterColumn                 | scala.Long                | counter           |
| StaticColumn&lt;type&gt;      | &lt;type&gt;              | type static       |


<a id="optional-primitive-columns">Optional primitive columns</a>
===================================================================
<a href="#table-of-contents">back to top</a>

Optional columns allow you to set a column to a ```null``` or a ```None```. Use them when you really want something to be optional.
The outcome is that instead of a ```T``` you get an ```Option[T]``` and you can ```match, fold, flatMap, map``` on a ```None```.

The ```Optional``` part is handled at a DSL level, it's not translated to Cassandra in any way.

| phantom columns               | Java/Scala type                   | Cassandra columns |
| ---------------               | -------------------------         | ----------------- |
| OptionalBlobColumn            | Option[java.nio.ByteBuffer]       | blog              |
| OptionalBigDecimalColumn      | Option[scala.math.BigDecimal]     | decimal           |
| OptionalBigIntColumn          | Option[scala.math.BigInt]         | varint            |
| OptionalBooleanColumn         | Option[scala.Boolean]             | boolean           |
| OptionalDateColumn            | Option[java.util.Date]            | timestamp         |
| OptionalDateTimeColumn        | Option[org.joda.time.DateTime]    | timestamp         |
| OptionalDoubleColumn          | Option[scala.Double]              | double            |
| OptionalEnumColumn            | Option[scala.Enumeration]         | text              |
| OptionalFloatColumn           | Option[scala.Float]               | float             |
| OptionalIntColumn             | Option[scala.Int]                 | int               |
| OptionalInetAddressColumn     | Option[java.net.InetAddress]      | inet              |
| OptionalLongColumn            | Option[Long]                      | long              |
| OptionalStringColumn          | Option[java.lang.String]          | text              |
| OptionalUUIDColumn            | Option[java.util.UUID]            | uuid              |
| OptionalTimeUUID              | Option[java.util.UUID]            | timeuuid          |


<a id="collection-columns">Collection columns</a>
======================================================
<a href="#table-of-contents">back to top</a>

Cassandra collections do not allow custom data types. Storing JSON as a string is possible, but it's still a ```text``` column as far as Cassandra is concerned.
The ```type``` in the below example is always a default C* type.

JSON columns require you to define a ```toJson``` and ```fromJson``` method, telling phantom how to go from a ```String``` to the type you need. 
It makes no assumptions as to what library you are using, although we have tested with ```lift-json``` and ```play-json```.

Examples on how to use JSON columns can be found in [JsonColumnTest.scala](https://github.com/websudos/phantom/blob/develop/phantom-dsl/src/test/scala/com/websudos/phantom/builder/query/db/specialized/JsonColumnTest.scala)

| phantom columns                     | Cassandra columns       |
| ---------------                     | -----------------       |
| ListColumn.&lt;type&gt;             | list&lt;type&gt;        |
| SetColumn.&lt;type&gt;              | set&lt;type&gt;         |
| MapColumn.&lt;type, type&gt;        | map&lt;type, type&gt;   |
| JsonColumn.&lt;type&gt;             | text                    |
| JsonListColumn.&lt;type&gt;         | list&lt;text&gt;        |
| JsonSetColumn.&lt;type&gt;          | set&lt;type&gt;         |

<a id="indexing-columns">Indexing columns</a>
==========================================
<a href="#table-of-contents">back to top</a>

phantom uses a specific set of traits to enforce more advanced Cassandra limitations and schema rules at compile time.
Instead of waiting for Cassandra to tell you you've done bad things, phantom won't let you compile them, saving you a lot of time.

The error messages you get when your model is off with respect to Cassandra rules is not particularly helpful and we are working on a better builder to allow
 for better error messages. Until then, if you see things like:

```scala

import com.websudos.phantom.dsl._

case class Student(id: UUID, name: String)

class Students extends CassandraTable[Students, Student] {
  object id extends UUIDColumn(this) with PartitionKey[UUID]
  object name extends StringColumn(this)

  def fromRow(row: Row): Student = {
    Student(id(row), name(row))
  }
}

object Students extends Students with Connector {

  /**
   * The below code will result in a compilation error phantom produces by design.
   * This behaviour is not only correct with respect to CQL but also intended by the implementation.
   *
   * The reason why it won't compile is because the "name" column is not an index in the "Students" table, which means using "name" in a "where" clause is
   * invalid CQL. Phantom prevents you from running most invalid queries by simply giving you a compile time error instead.
   */
  def getByName(name: String): Future[Option[Student]] = {
    select.where(_.name eqs name).one()
  }
}
```

The compilation error message for the above looks something like this:

```scala
 value eqs is not a member of object x$9.name
```

Might seem overly mysterious to start with, but the logic is simple. There is no implicit conversion in scope to convert your non-indexed column to a ```QueryColumn```. If you don't have an index, you can't query.

```scala
  Students.update.where(_.id eqs someId).onlyIf(_.name is "test")
```


<a id="partition-key">PartitionKey</a>
==============================================
<a href="#table-of-contents">back to top</a>

This is the default partitioning key of the table, telling Cassandra how to divide data into partitions and store them accordingly.
You must define at least one partition key for a table. Phantom will gently remind you of this with a fatal error.

If you use a single partition key, the ```PartitionKey``` will always be the first ```PrimaryKey``` in the schema.

It looks like this in CQL: ```PRIMARY_KEY(your_partition_key, primary_key_1, primary_key_2)```.

Using more than one ```PartitionKey[T]``` in your schema definition will output a Composite Key in Cassandra.
```PRIMARY_KEY((your_partition_key_1, your_partition_key2), primary_key_1, primary_key_2)```.


<a id="primary-key">PrimaryKey</a>
==============================================
<a href="#table-of-contents">back to top</a>

As it's name says, using this will mark a column as ```PrimaryKey```. Using multiple values will result in a Compound Value.
The first ```PrimaryKey``` is used to partition data. phantom will force you to always define a ```PartitionKey``` so you don't forget
about how your data is partitioned. We also use this DSL restriction because we hope to do more clever things with it in the future.

A compound key in C* looks like this:
```PRIMARY_KEY(primary_key, primary_key_1, primary_key_2)```.

Before you add too many of these, remember they all have to go into a ```where``` clause.
You can only query with a full primary key, even if it's compound. phantom can't yet give you a compile time error for this, but Cassandra will give you a runtime one.

<a id="secondary-index">SecondaryIndex</a>
==============================================
<a href="#table-of-contents">back to top</a>

This is a SecondaryIndex in Cassandra. It can help you enable querying really fast, but it's not exactly high performance.
It's generally best to avoid it, we implemented it to show off what good guys we are.

When you mix in ```Index[T]``` on a column, phantom will let you use it in a ```where``` clause.
However, don't forget to ```allowFiltering``` for such queries, otherwise C* will give you an error.

<a id="clustering-order">ClusteringOrder</a>
=================================================
<a href="#table-of-contents">back to top</a>

This can be used with either ```java.util.Date``` or ```org.joda.time.DateTime```. It tells Cassandra to store records in a certain order based on this field.

An example might be: ```object timestamp extends DateTimeColumn(this) with ClusteringOrder[DateTime] with Ascending```
To fully define a clustering column, you MUST also mixin either ```Ascending``` or ```Descending``` to indicate the sorting order.



<a id="thrift-columns">Thrift Columns</a>
==========================================
<a href="#table-of-contents">back to top</a>

These columns are especially useful if you are building Thrift services. They are deeply integrated with Twitter Scrooge and relevant to the Twitter ecosystem(Finagle, Zipkin, Storm etc)
They are available via the ```phantom-thrift``` module and you need to import the Thrift package to get all necessary types into scope.

 ```scala
 import com.websudos.phantom.thrift._
 ```

In the below scenario, the Cassandra type is always text and the type you need to pass to the column is a Thrift struct, specifically ```com.twitter.scrooge
.ThriftStruct```.
phantom will use a ```CompactThriftSerializer```, store the record as a binary string and then reparse it on fetch.

Thrift serialization and de-serialization is extremely fast, so you don't need to worry about speed or performance overhead.
You generally use these to store collections(small number of items), not big things.

| phantom columns                     | Cassandra columns       |
| ---------------                     | -----------------       |
| ThriftColumn.&lt;type&gt;           | text                    |
| ThriftListColumn.&lt;type&gt;       | list&lt;text&gt;        |
| ThriftSetColumn.&lt;type&gt;        | set&lt;text&gt;         |
| ThriftMapColumn.&lt;type, type&gt;  | map&lt;text, text&gt;   |



<a id="data-modeling">Data modeling with phantom</a>
====================================================
<a href="#table-of-contents">back to top</a>

```scala

import java.util.Date
import com.websudos.phantom.sample.ExampleModel
import com.websudos.phantom.dsl._

case class ExampleModel (
  id: Int,
  name: String,
  props: Map[String, String],
  timestamp: Int,
  test: Option[Int]
)

sealed class ExampleRecord extends CassandraTable[ExampleRecord, ExampleModel] {

  object id extends UUIDColumn(this) with PartitionKey[UUID]
  object timestamp extends DateTimeColumn(this) with ClusteringOrder[DateTime] with Ascending
  object name extends StringColumn(this)
  object props extends MapColumn[ExampleRecord, ExampleModel, String, String](this)
  object test extends OptionalIntColumn(this)

  def fromRow(row: Row): ExampleModel = {
    ExampleModel(id(row), name(row), props(row), timestamp(row), test(row));
  }
}

```

<a id="querying-with-phantom">Querying with Phantom</a>
=======================================================
<a href="#table-of-contents">back to top</a>

The query syntax is inspired by the Foursquare Rogue library and aims to replicate CQL 3 as much as possible.

Phantom works with both Scala Futures and Twitter Futures as first class citizens.


<a id="common-query-methods">Common query methods</a>
=====================================================
<a href="#table-of-contents">back to top</a>

The full list can be found in [CQLQuery.scala](https://github.com/websudos/phantom/blob/develop/phantom-dsl/src/main/scala/com/websudos/phantom/query/CQLQuery
.scala).

| Method name                       | Description                                                                           |
| --------------------------------- | ------------------------------------------------------------------------------------- |
| ```tracing_=```                   | The Cassandra utility method. Enables or disables tracing.                            |
| ```queryString```                 | Get the output CQL 3 query of a phantom query.                                        |
| ```consistencyLevel```            | Retrieves the consistency level in use.                                               |
| ```consistencyLevel_=```          | Sets the consistency level to use.                                                    |
| ```retryPolicy```                 | Retrieves the RetryPolicy in use.                                                     |
| ```retryPolicy_=```               | Sets the RetryPolicy to use.                                                          |
| ```serialConsistencyLevel```      | Retrieves the serial consistency level in use.                                        |
| ```serialConsistencyLevel_=```    | Sets the serial consistency level to use.                                             |
| ```forceNoValues_=```             | Sets the serial consistency level to use.                                             |
| ```routingKey```                  | Retrieves the Routing Key as a ByteBuffer.                                            |


<a id="select-queries">Select queries</a>
================================================
<a href="#table-of-contents">back to top</a>

| Method name                       | Description                                                                           |
| --------------------------------- | ------------------------------------------------------------------------------------- |
| ```where```                       | The ```WHERE``` clause in CQL                                                         |
| ```and```                         | Chains several clauses, creating a ```WHERE ... AND``` query                          |
| ```orderBy```                     | Adds an ```ORDER_BY column_name``` to the query                                       |
| ```allowFiltering```              | Allows Cassandra to filter records in memory. This is an expensive operation.         |
| ```limit```                       | Sets the exact number of records to retrieve.                                         |


Select queries are very straightforward and enforce most limitations at compile time.


<a id="where-and-operators">```where``` and ```and``` clause operators</a>
==========================================
<a href="#table-of-contents">back to top</a>

| Operator name      | Description                                                              |
| ------------------ | ------------------------------------------------------------                                             |
| eqs                | The "equals" operator. Will match if the objects are equal                                               |
| in                 | The "in" operator. Will match if the object is found the list of arguments                               |
| gt                 | The "greater than" operator. Will match a the record is greater than the argument and exists             |
| gte                | The "greater than or equals" operator. Will match a the record is greater than the argument and exists   |
| lt                 | The "lower than" operator. Will match a the record that is less than the argument and exists             |
| lte                | The "lower than or equals" operator. Will match a the record that is less than the argument and exists   |


<a id="partial-select-queries">Partial selects</a>
===================================================
<a href="#table-of-contents">back to top</a>

All partial select queries will return Tuples and are therefore limited to 22 fields.
We haven't yet bothered to add more than 10 fields in the select, but you can always do a Pull Request.
The file you are looking for is [here](https://github.com/websudos/phantom/blob/develop/phantom-dsl/src/main/scala/com/websudos/phantom/SelectTable.scala).
The 22 field limitation will change in Scala 2.11 and phantom will be updated once cross version compilation is enabled.

```scala
  def getNameById(id: UUID): Future[Option[String]] = {
    ExampleRecord.select(_.name).where(_.id eqs someId).one()
  }

  def getNameAndPropsById(id: UUID): Future[Option(String, Map[String, String])] {
    ExampleRecord.select(_.name, _.props).where(_.id eqs someId).one()
  }
```

<a id="insert-queries">"Insert" queries</a>
==========================================
<a href="#table-of-contents">back to top</a>

| Method name                       | Description                                                                           |
| --------------------------------- | ------------------------------------------------------------------------------------- |
| ```value```                       | A type safe Insert query builder. Throws an error for ```null``` values.              |
| ```valueOrNull```                 | This will accept a ```null``` without throwing an error.                              |
| ```ttl```                         | Sets the "Time-To-Live" for the record.                                               |


<a id="update-queries">"Update" queries</a>
==========================================
<a href="#table-of-contents">back to top</a>

| Method name                       | Description                                                                           |
| --------------------------------- | ------------------------------------------------------------------------------------- |
| ```where```                       | The ```WHERE``` clause in CQL                                                         |
| ```and```                         | Chains several clauses, creating a ```WHERE ... AND``` query                          |
| ```modify```                      | The actual update query builder                                                       |
| ```onlyIf```                     | Addition update condition. Used on non-primary columns                                |

Example:

```scala
ExampleRecord.update
  .where(_.id eqs myUuid)
  .modify(_.name setTo "Barack Obama")
  .and(_.props put ("title" -> "POTUS"))
  .future()
```


<a id="delete-queries">"Delete" queries</a>
===========================================
<a href="#table-of-contents">back to top</a>

| Method name                       | Description                                                                           |
| --------------------------------- | ------------------------------------------------------------------------------------- |
| ```where```                       | The ```WHERE``` clause in CQL                                                         |
| ```and```                         | Chains several clauses, creating a ```WHERE ... AND``` query                          |


Delete queries are very simple ways to either delete a row or alternatively set a column to ```null```. For instance:


```scala

BasicTable.update.where(_.id eqs someId).modify(_.someSet setTo Set.empty[String])

// is actually equivalent to

BasicTable.delete(_.someSet).where(_.id eqs someId)

```



<a id="query-api">Query API</a>
===============================

Phantom offers a dual query API based on Scala concurrency primitives, which makes it trivial to use phantom in most known frameworks, such as Play!, Spray,
Akka, Scruffy, Lift, and many others. Integration is trivial and easily achievable, all you have to do is to use the Scala API methods and you get out of the
 box integration.

Phantom also offers another API based on Twitter proprietary concurrency primitives. This is due to the fact that internally we rely very heavily on the
Twitter eco-system. It's why phantom also offers Finagle-Thrift support out of the box and integrates with Twitter Scrooge. It fits in perfectly with
applications powered by Finagle RPC, Zipkin, Thrift, Ostrich, Aurora, Mesos, and the rest of the Twitter lot.


| Method name                        | Description                                                                           | Scala result type |
| ---------------------------------- | ------------------------------------------------------------------------------------- | ------------------|
| ```future```                       | Executes a command and returns a ```ResultSet```. This is useful when you don't need to return a value.| ```scala.concurrent.Future[ResultSet]``` |
| ```execute```                       | Executes a command and returns a ```ResultSet```. This is useful when you don't need to return a value.| ```com.twitter.util.Future[ResultSet]``` |
| ```one```                          | Executes a command and returns an ```Option[T]```. Use this when you are selecting and you only need one value. Adds ```LIMIT 1``` to the CQL query. | ```scala.concurrent.Future[Option[Record]]``` |
| ```get```                          | Executes a command and returns an ```Option[T]```. Use this when you are selecting and you only need one value. Adds```LIMIT 1``` to the CQL query. | ```com.twitter.util.Future[Option[Record]]``` |
| ```fetch```                          | Returns a sequence of matches. Use when you expect more than 1 match. | ```scala.concurrent.Future[Seq[Record]]``` |.
| ```collect```                          |  Returns a sequence of matches. Use when you expect more than 1 match. | ```com.twitter.util.Future[Seq[Record]``` |
| ```fetchSpool```                        | This is useful when you need the underlying ResultSpool.                        | ```com.twitter.concurrent.Spool[T]]``` |
| ```fetchEnumerator```                        | This is useful when you need the underlying Play based enumerator.                        | ```play.api.libs.iteratee.Enumerator[T]``` |





<a id="scala-futures">Scala Futures</a>
=======================================
<a href="#table-of-contents">back to top</a>


Phantom offers a dual asynchronous Future API for the completion of tasks, ```scala.concurrent.Future``` and ```com.twitter.util.Future```.
However, the concurrency primitives are all based on Google Guava executors and listening decorators. The future API is just for the convenience of users.
The Scala Future methods are: 


```scala
ExampleRecord.select.one() // When you only want to select one record
ExampleRecord.update.where(_.name eqs name).modify(_.name setTo "someOtherName").future() // When you don't care about the return type.
ExampleRecord.select.fetchEnumerator // when you need an Enumerator
ExampleRecord.select.fetch // When you want to fetch a Seq[Record]
```

<a id="scala-futures-examples">Examples with Scala Futures</a>
================================================================
<a href="#table-of-contents">back to top</a>


```scala

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object ExampleRecord extends ExampleRecord {
  override val tableName = "examplerecord"

  // now define a session, a normal Datastax cluster connection
  implicit val session = SomeCassandraClient.session;

  def getRecordsByName(name: String): Future[Seq[ExampleModel]] = {
    ExampleRecord.select.where(_.name eqs name).fetch
  }

  def getOneRecordByName(name: String, someId: UUID): Future[Option[ExampleModel]] = {
    ExampleRecord.select.where(_.name eqs name).and(_.id eqs someId).one()
  }
}
```

<a id="twitter-futures">Twitter Futures</a>
===========================================
<a href="#table-of-contents">back to top</a>

Phantom doesn't depend on Finagle for this, we are simply using ```"com.twitter" %% "util-core" % Version"``` to return a ```com.twitter.util.Future```. 
However, the concurrency primitives are all based on Google Guava executors and listening decorators. The future API is just for the convenience of users.


```scala
ExampleRecord.select.get() // When you only want to select one record
ExampleRecord.update.where(_.name eqs name).modify(_.name setTo "someOtherName").execute() // When you don't care about the return type.
ExampleRecord.select.enumerate // when you need an Enumerator
ExampleRecord.select.collect // When you want to fetch a Seq[Record]
```

<a id="twitter-futures-examples">More examples with Twitter Futures</a>
=======================================================================
<a href="#table-of-contents">back to top</a>

```scala

import com.twitter.util.Future

object ExampleRecord extends ExampleRecord {
  override val tableName = "examplerecord"

  // now define a session, a normal Datastax cluster connection
  implicit val session = SomeCassandraClient.session;

  def getRecordsByName(name: String): Future[Seq[ExampleModel]] = {
    ExampleRecord.select.where(_.name eqs name).collect
  }

  def getOneRecordByName(name: String, someId: UUID): Future[Option[ExampleModel]] = {
    ExampleRecord.select.where(_.name eqs name).and(_.id eqs someId).get()
  }
}
```

<a id="collections-and-operators">Collections and operators</a>
================================================================
<a href="#table-of-contents">back to top</a>

Based on the above list of columns, phantom supports CQL 3 modify operations for CQL 3 collections: ```list, set, map```.
All operators will be available in an update query, specifically:

```ExampleRecord.update.where(_.id eqs someId).modify(_.someList $OPERATOR $args).future()```.

<a id="list-operators">List operators</a>
==========================================
<a href="#table-of-contents">back to top</a>

Examples in [ListOperatorsTest.scala](https://github.com/websudos/phantom/blob/develop/phantom-dsl/src/test/scala/com/websudos/phantom/dsl/crud/ListOperatorsTest.scala).

| Name                          | Description                                   |
| ----------------------------- | --------------------------------------------- |
| ```prepend```                 | Adds an item to the head of the list          |
| ```prependAll```              | Adds multiple items to the head of the list   |
| ```append```                  | Adds an item to the tail of the list          |
| ```appendAll```               | Adds multiple items to the tail of the list   |
| ```discard```                 | Removes the given item from the list.         |
| ```discardAll```              | Removes all given items from the list.        |
| ```setIdIx```                 | Updates a specific index in the list          |

<a id="set-operators">Set operators</a>
=======================================
<a href="#table-of-contents">back to top</a>

Sets have a better performance than lists, as the Cassandra documentation suggests.
Examples in [SetOperationsTest.scala](https://github.com/websudos/phantom/blob/develop/phantom-test/src/test/scala/com/websudos/phantom/dsl/crud/SetOperationsTest.scala).

| Name                          | Description                                   |
| ----------------------------- | --------------------------------------------- |
| ```add```                     | Adds an item to the tail of the set           |
| ```addAll```                  | Adds multiple items to the tail of the set    |
| ```remove ```                 | Removes the given item from the set.          |
| ```removeAll```               | Removes all given items from the set.         |


<a id="map-operators">Map operators</a>
=======================================
<a href="#table-of-contents">back to top</a>

Both the key and value types of a Map must be Cassandra primitives.
Examples in [MapOperationsTest.scala](https://github.com/websudos/phantom/blob/develop/phantom-test/src/test/scala/com/websudos/phantom/dsl/crud/MapOperationsTest.scala):

| Name                          | Description                                   |
| ----------------------------- | --------------------------------------------- |
| ```put```                     | Adds an (key -> value) pair to the map        |
| ```putAll```                  | Adds multiple (key -> value) pairs to the map |


<a id="automated-schema-generation">Automated schema generation</a>
===================================================================
<a href="#table-of-contents">back to top</a>

Replication strategies and more advanced features are not yet available in phantom, but CQL 3 Table schemas are  automatically generated from the Scala code. To create a schema in Cassandra from a table definition:

```scala

import scala.concurrent.Await
import scala.concurrent.duration._

Await.result(ExampleRecord.create().future(), 5000 millis)
```

Of course, you don't have to block unless you want to.


<a id="partition-tokens">Partition tokens</a>
==============================================
<a href="#table-of-contents">back to top</a>

```scala

import scala.concurrent.Await
import scala.concurrent.duration._
import com.websudos.phantom.dsl._

sealed class ExampleRecord2 extends CassandraTable[ExampleRecord2, ExampleModel] {

  object id extends UUIDColumn(this) with PartitionKey[UUID]
  object order_id extends LongColumn(this) with ClusteringOrder[Long] with Descending
  object timestamp extends DateTimeColumn(this)
  object name extends StringColumn(this)
  object props extends MapColumn[ExampleRecord2, ExampleRecord, String, String](this)
  object test extends OptionalIntColumn(this)

  override def fromRow(row: Row): ExampleModel = {
    ExampleModel(id(row), name(row), props(row), timestamp(row), test(row));
  }
}


val orderedResult = Await.result(Articles.select.where(_.id gtToken one.get.id ).fetch, 5000 millis)

```

<a id="partition-token-operators">PartitionToken operators</a>
===============================================================
<a href="#table-of-contents">back to top</a>

| Operator name      | Description                                                              |
| ------------------ | ------------------------------------------------------------                                             |
| eqsToken           | The "equals" operator. Will match if the objects are equal                                               |
| gtToken            | The "greater than" operator. Will match a the record is greater than the argument                        |
| gteToken           | The "greater than or equals" operator. Will match a the record is greater than the argument              |
| ltToken            | The "lower than" operator. Will match a the record that is less than the argument and exists             |
| lteToken           | The "lower than or equals" operator. Will match a the record that is less than the argument              |

For more details on how to use Cassandra partition tokens, see [SkipRecordsByToken.scala]( https://github.com/websudos/phantom/blob/develop/phantom-test/src/test/scala/com/websudos/phantom/dsl/SkipRecordsByToken.scala)


<a id="time-series">Cassandra Time Series</a>
=============================================
<a href="#table-of-contents">back to top</a>

phantom supports Cassandra Time Series. To use them, simply mixin ```com.websudos.phantom.keys.ClusteringOrder``` and either ```Ascending``` or ```Descending```.

Restrictions are enforced at compile time.

```scala

import com.websudos.phantom.dsl._

sealed class ExampleRecord3 extends CassandraTable[ExampleRecord3, ExampleModel] with LongOrderKey[ExampleRecod3, ExampleRecord] {

  object id extends UUIDColumn(this) with PartitionKey[UUID]
  object timestamp extends DateTimeColumn(this) with ClusteringOrder[DateTime] with Ascending
  object name extends StringColumn(this)
  object props extends MapColumn[ExampleRecord2, ExampleRecord, String, String](this)
  object test extends OptionalIntColumn(this)

  override def fromRow(row: Row): ExampleModel = {
    ExampleModel(id(row), name(row), props(row), timestamp(row), test(row));
  }
}
```

Automatic schema generation can do all the setup for you.


<a id="compound-keys">Compound keys</a>
=======================================
<a href="#table-of-contents">back to top</a>

Phantom also supports using Compound keys out of the box. The schema can once again by auto-generated.

A table can have only one ```PartitionKey``` but several ```PrimaryKey``` definitions. Phantom will use these keys to build a compound value. Example scenario, with the compound key: ```(id, timestamp, name)```

```scala

import com.websudos.phantom.dsl._

sealed class ExampleRecord3 extends CassandraTable[ExampleRecord3, ExampleModel] {

  object id extends UUIDColumn(this) with PartitionKey[UUID]
  object order_id extends LongColumn(this) with ClusteringOrder[Long] with Descending
  object timestamp extends DateTimeColumn(this) with PrimaryKey[DateTime]
  object name extends StringColumn(this) with PrimaryKey[String]
  object props extends MapColumn[ExampleRecord2, ExampleRecord, String, String](this)
  object test extends OptionalIntColumn(this)

  override def fromRow(row: Row): ExampleModel = {
    ExampleModel(id(row), name(row), props(row), timestamp(row), test(row));
  }
}
```

<a id="secondary-keys">CQL 3 Secondary Keys</a>
===============================================
<a href="#table-of-contents">back to top</a>

When you want to use a column in a ```where``` clause, you need an index on it. Cassandra data modeling is out of the scope of this writing, 
but phantom offers ```com.websudos.phantom.keys.Index``` to enable querying.

The CQL 3 schema for secondary indexes can also be auto-generated with ```ExampleRecord4.create()```.

```SELECT``` is the only query you can perform with an ```Index``` column. This is a Cassandra limitation. The relevant tests are found [here](https://github.com/websudos/phantom/blob/develop/phantom-test/src/test/scala/com/websudos/phantom/dsl/specialized/SecondaryIndexTest.scala).


```scala

import com.websudos.phantom.dsl._

sealed class ExampleRecord4 extends CassandraTable[ExampleRecord4, ExampleModel] {

  object id extends UUIDColumn(this) with PartitionKey[UUID]
  object order_id extends LongColumn(this) with ClusteringOrder[Long] with Descending
  object timestamp extends DateTimeColumn(this) with Index[DateTime]
  object name extends StringColumn(this) with Index[String]
  object props extends MapColumn[ExampleRecord2, ExampleRecord, String, String](this)
  object test extends OptionalIntColumn(this)

  override def fromRow(row: Row): ExampleModel = {
    ExampleModel(id(row), name(row), props(row), timestamp(row), test(row));
  }
}
```

<a id="async-iterators">Asynchronous iterators for large record sets</a>
========================================================================
<a href="#table-of-contents">back to top</a>

Phantom comes packed with CQL rows asynchronous lazy iterators to help you deal with billions of records.
phantom iterators are based on Play iterators with very lightweight integration.

The functionality is identical with respect to asynchronous, lazy behaviour and available methods.
For more on this, see this [Play tutorial](
http://mandubian.com/2012/08/27/understanding-play2-iteratees-for-normal-humans/)


Usage is trivial. If you want to use ```slice, take or drop``` with iterators, the partitioner needs to be ordered.

```scala

import scala.concurrent.Await
import scala.concurrent.duration._
import com.websudos.phantom.dsl._


sealed class ExampleRecord3 extends CassandraTable[ExampleRecord3, ExampleModel] {

  object id extends UUIDColumn(this) with PartitionKey[UUID]
  object order_id extends LongColumn(this) with ClusteringOrder[Long] with Descending
  object timestamp extends DateTimeColumn(this) with PrimaryKey[DateTime]
  object name extends StringColumn(this) with PrimaryKey[String]
  object props extends MapColumn[ExampleRecord2, ExampleRecord, String, String](this)
  object test extends OptionalIntColumn(this)

  override def fromRow(row: Row): ExampleModel = {
    ExampleModel(id(row), name(row), props(row), timestamp(row), test(row));
  }
}

object ExampleRecord3 extends ExampleRecord3 {
  def getRecords(start: Int, limit: Int): Future[Set[ExampleModel]] = {
    select.fetchEnumerator.slice(start, limit).collect
  }
}

```

<a id="batch-statements">Batch statements</a>
=============================================
<a href="#table-of-contents">back to top</a>

phantom also brrings in support for batch statements. To use them, see [IterateeBigTest.scala](https://github.com/websudos/phantom/blob/develop/phantom-dsl/src/test/scala/com/websudos/phantom/iteratee/IterateeBigTest.scala)

We have tested with 10,000 statements per batch, and 1000 batches processed simultaneously. Before you run the test, beware that it takes ~40 minutes.

Batches use lazy iterators and daisy chain them to offer thread safe behaviour. They are not memory intensive and you can expect consistent processing speed even with 1 000 000 statements per batch.

Batches are immutable and adding a new record will result in a new Batch, just like most things Scala, so be careful to chain the calls.

phantom also supports COUNTER batch updates and UNLOGGED batch updates.


<a id="logged-batch-statements">LOGGED batch statements</a>
===========================================================
<a href="#table-of-contents">back to top</a>

```scala

import com.websudos.phantom.dsl._

Batch.logged
    .add(ExampleRecord.update.where(_.id eqs someId).modify(_.name setTo "blabla"))
    .add(ExampleRecord.update.where(_.id eqs someOtherId).modify(_.name setTo "blabla2"))
    .future()

```

<a id="counter-batch-statements">COUNTER batch statements</a>
============================================================
<a href="#table-of-contents">back to top</a>

```scala

import com.websudos.phantom.dsl._

Batch.counter
    .add(ExampleRecord.update.where(_.id eqs someId).modify(_.someCounter increment 500L))
    .add(ExampleRecord.update.where(_.id eqs someOtherId).modify(_.someCounter decrement 300L))
    .future()
```

<a id="unlogged-batch-statements">UNLOGGED batch statements</a>
============================================================
<a href="#table-of-contents">back to top</a>

```scala

import com.websudos.phantom.dsl._

Batch.unlogged
    .add(ExampleRecord.update.where(_.id eqs someId).modify(_.name setTo "blabla"))
    .add(ExampleRecord.update.where(_.id eqs someOtherId).modify(_.name setTo "blabla2"))
    .future()

```

<a id="thrift-integration">Thrift integration</a>
=================================================
<a href="#table-of-contents">back to top</a>

We use Apache Thrift extensively for our backend services. phantom is very easy to integrate with Thrift models and uses ```Twitter Scrooge``` to compile them. 
Thrift integration is optional and available via ```"com.websudos" %% "phantom-thrift"  % phantomVersion```.

```thrift
namespace java com.websudos.phantom.sample.ExampleModel

stuct ExampleModel {
  1: required i32 id,
  2: required string name,
  3: required Map&lt;string, string&gt; props,
  4: required i32 timestamp
  5: optional i32 test
}
```

<a id="apache-zookeeper-integration">Apache ZooKeeper Integration</a>
==================================================
<a href="#table-of-contents">back to top</a>

If you have never heard of Apache ZooKeeper before, a much better place to start is [here](http://zookeeper.apache.org/). Phantom offers a complete set of features for ZooKeeper integration using the [finagle-zookeeper](https://github.com/p-antoine/finagle-zookeeper) project.


<a id="zookeeper-connectors">ZooKeeper Connectors</a>
===========================================================================
<a href="#table-of-contents">back to top</a>

Using a set of conventions phantom can automate the entire process of using ZooKeeper in a distributed environment. Phantom will deal with a large series of concerns for you, specifically:

- Creating a ZooKeeper client and initialising it in due time.
- Fetching and parsing a sequence of Cassandra ports from ZooKeeper.
- Creating a Cluster configuration based on the sequence of Cassandra ports available in ZooKeeper.
- Creating an implicit session for queries to execute.

The entire process described above is entirely automated with a series of sensible defaults available. More details on default implementations are available below. Bottom line, if you want to go custom, you may override at will, if you just want to get something working as fast as possible, then ```phantom-zookeeper``` can do everything for you.

<a id="the-simple-cassandra-connector">The simple Cassandra Connector</a>
==========================================================================================================

This implementation is a very simple way to connect to a running Cassandra node. This is not using ZooKeeper and it's not really indented for multi-node 
testing or connections, but sometimes you just want to get things working immediately.

The implementation details are available [here](https://github
.com/websudos/phantom/blob/develop/phantom-connectors/src/main/scala/com/websudos/phantom/connectors/SimpleCassandraConnector.scala),
but without further ado, this connector will attempt to connector to a local Cassandra, either embedded or not.

Inside Websudos, our port convention is ```9042``` for local Cassandra and ```9142``` for embedded. This is reflected in our ```cassandra.yaml``` 
configuration files. Overidding this is quite simple, although you will need to create your own pair of manager and connector.


<a id="the-default-zookeeper-connector-and-default-zookeeper-mananager">The DefaultZooKeeperConnector and DefaultZooKeeperManager</a>
==========================================================================================================
<a href="#table-of-contents">back to top</a>

The default implementation expects Cassandra IPs to be listed in a Sequence of ```host:port``` combinations, with ```:``` as a separator literal. It also expects the default path in ZooKeeper for Cassandra ports to be ```/cassandra``` and the sequence of ports should look like this:

```host1:port1, host2:port2, host3:port3, host4:port4```

Phantom will fetch the data found on the  ```/cassandra``` path on the ZooKeeper master and attempt to parse all ```host:port``` pairs to a ```Seq[InetSocketAddress]``` and build a ```com.datastax.driver.core.Cluster``` using the sequence of addresses.

Using that ```Cluster``` phantom will spawn an ```implicit session: com.datastax.driver.core.Session```. This session is the execution context of all queries inside a table definition. The ```DefaultZooKeeperManager```, found [here](https://github.com/websudos/phantom/blob/develop/phantom-zookeeper/src/main/scala/com/websudos/phantom/zookeeper/ZookeeperManager.scala), will do all the plumbing work for you. More details on the internals are available [here](https://github.com/websudos/phantom/blob/develop/phantom-zookeeper/src/main/scala/com/websudos/phantom/zookeeper/ZookeeperManager.scala#L51).

<a id="testing-utilities">phantom-testkit</a>
==================================================
<a href="#table-of-contents">back to top</a>

Naturally, no job is considered truly done with the full power testing automation provided out-of-the box. This is exactly what we tried to achieve with the 
testing utilities, giving you a very simple, easily extensible, yet highly sensible defaults. We wanted something that works for most things most of the time
with 0 integration work on your behalf, yet allowing you to go crazy and custom as you please if the scenario warrants it. 

With that design philosophy in mind, we've created two kinds of tests, 1 running with a ```SimpleCassandraConnector```, 
with the implementation found [here](https://github.com/websudos/phantom/blob/develop/phantom-testkit/src/main/scala/com/websudos/phantom/testkit/suites/SimpleCassandraConnector.scala), where the testing utilities will auto-spawn an Embedded Cassandra database with the right version and the right settings,
run all the tests and cleanup after tests are done.

The other, more complex implementation, targets users who want to use phantom/Cassandra in a distributed environment. This is an easy way to automate 
multi-DC or multi-cluster tests via service discovery with Apache ZooKeeper. More details are available right above. The ```BaseTest``` implementation, 
which uses a ```DefaultZooKeeperConnector```, is found [here](https://github.com/websudos/phantom/blob/develop/phantom-testkit/src/main/scala/com/websudos/phantom/testkit/suites/BaseTest.scala), and it follows the pattern described above.


There are 4 core implementations available:


| Name    | Description                                                                         | ZooKeeper support | Auto-embedding support |
| ------- | --------------------------------------------------------------------------------------------------------- | ----------- | ---------------------- |
| CassandraFlatSpec             | Simple FlatSpec trait mixin, based on ```org.scalatest.FlatSpec```                  | No          | Yes                    |
| CassandraFeatureSpec          | Simple FeatureSpec trait mixin, based on ```org.scalatest.FeatureSpec```            | No          | Yes                    |
| BaseTest                      | ZooKeeper powered FlatSpec trait mixin, based on ```org.scalatest.FlatSpec```       | Yes         | Yes                    |
| FeatureBestTest               | ZooKeeper powered FeatureSpec trait mixin, based on ```org.scalatest.FeatureSpec``` | Yes         | Yes                    |

 
 
Using the built in testing utilities is very simple. In most cases, you use one of the first two base implementations, 
either ```CassandraFlatSpec``` or ```CassandraFeatureSpec```, based on what kind of tests you like writing(flat or feature).


To get started with phantom tests, the usual steps are as follows:

- Create a global method to initialise all your tables using phantom's auto-generation capability.
- Create a global method to cleanup and truncate your tables after tests finish executing.
- Create a root specification file that you plan to use for all your tests.


```scala

import scala.concurrent.{ Await, Future }
import scala.concurrent.duration._
import com.websudos.phantom.dsl._


object DatabaseService {
  def init(): Future[List[ResultSet]] = {
    val create = Future.sequence(List(
      Table1.create.future(),
      Table2.create.future()
    ))

    Await.ready(create, 5.seconds)
  }

   def cleanup(): Future[List[ResultSet]] = {
    val truncate = Future.sequence(List(
      Table1.truncate.future(),
      Table2.truncate.future()
    ))
    Await.ready(truncate, 5.seconds)
  }
}

import com.websudos.phantom.testkit._

trait CustomSpec extends CassandraFlatSpec {

   override def beforeAll(): Unit = {
     super.beforeAll()
     DatabaseService.init()
   }

   override def afterAll(): Unit = {
     super.afterAll()
     DatabaseService.cleanup()
   }
}
```

Running your database tests with phantom is now trivial. A great idea is to use asynchronous testing patterns and future sequencers to get the best possible
performance even out of your tests. Now all your other test suites that need a running database would look like this:

```scala

import com.websudos.phantom.dsl._
import com.websudos.util.testing._

class UserDatabaseServiceTest extends CustomSpec {
  it should "register a user from a model" in {
    val user = //.. create a user

    // A for-yield will get de-sugared to a flatMap chain, but in effect you get a sequence that says:
    // First write, then fetch by id. The beauty of it is the first future will only complete when the user has been written
    // So you have an async sequence guarantee that the "getById" will be done only after the user is actually available.
    val chain = for {
      store <- UserDatabaseService.register(user)
      get <- UserDatabaseService.getById(user.id)
    } yield get

    // The "successful" method comes from com.websudos.util.testing._ in our util project.
    chain.successful {
      result => {

        // result is now Option[User]

        result.isDefined shouldEqual true
        result.get shouldEqual user
      }
    }
  }
}

```



If you are using ZooKeeper and you want to run tests through a full ZooKeeper powered cycle, where Cassandra settings are retrieved from a ZooKeeper that 
can either be running locally or auto-spawned if none is found, pick one of the last two base suites.
 
 

<a id="auto-embedded-cassandra">Auto-embedded Cassandra</a>
===========================================================
<a href="#table-of-contents">back to top</a>

Phantom spares you of the trouble to spawn your own Cassandra server during tests. The implementation of this is based on the [cassandra-unit]
(https://github.com/jsevellec/cassandra-unit) project. Phantom will automatically pick the right version of Cassandra, 
however do be careful. We often tend to use the latest version as we do our best to keep up with the latest features.

You may use a brand new phantom feature, see the tests passing with flying colours locally and then get bad errors in production. The version of Cassandra 
covered by the latest phantom release and used for embedding is written at the very top of this readme.

<a id="running-the-tests-locally">Running the tests locally</a>
==================================================
<a href="#table-of-contents">back to top</a>

phantom uses the ```phantom-testkit``` module to run tests without a local Cassandra server running.
There are no pre-requisites for running the tests. Phantom will automatically load an Embedded Cassandra with the right version, 
run all the tests and do the cleanup afterwards. Read more on the testing utilities to see how you can achieve the same thing in your own database tests.

If a local Cassandra installation is found running on ```localhost:9042```, phantom will attempt to use that instead. Some of the version based logic
is found directly inside phantom, although advanced compatibility and protocol version detection has been a task we left to our dear partners at Datastax
as we've felt re-implementing that concern in Scala would bring no significant value add.

Phantom uses multiple SBT configurations to distinguish between two kinds of tests, normal and performance tests. Performance tests are not run
during Travis CI runs and we usually run them manually when serious changes are made to the underlying Twitter Spool and Play Iterator based iterators, events that are very rare indeed.

- Use ```sbt test``` to run the normal test suite which should finish pretty quickly, within 2 minutes. 
- Use ```sbt perf:test``` if you have a lot of time on your hands and you are debugging performance issues with the framework. This will take 40 -50 minutes.


<a id="contributors">Contributors</a>
=====================================
<a href="#table-of-contents">back to top</a>

Phantom was developed at websudos as an in-house project. All Cassandra integration at Websudos goes through phantom, and nowadays it's safe to say most
Scala/Cassandra users in the world rely on phantom.

* Flavian Alexandru ([@alexflav23](https://github.com/alexflav23)) - maintainer
* Viktor Taranenko ([@viktortnk](https://github.com/viktortnk))
* Bartosz Jankiewicz ([@bjankie1](https://github.com/bjankie1)
* Eugene Zhulenev ([@ezhulenev](https://github.com/ezhulenev)
* Benjamin Edwards ([@benjumanji](https://github.com/benjumanji)
* Stephen Samuel ([@sksamuel](https://github.com/sksamuel)
* Tomasz Perek ([@tperek](https://github.com/tperek)
* Evan Chan ([@evanfchan](https://github.com/evanfchan)

<a id="copyright">Copyright</a>
===============================
<a href="#table-of-contents">back to top</a>

Special thanks to Viktor Taranenko from WhiskLabs, who gave us the original idea.

Copyright 2013 - 2015 websudos.


Contributing to phantom
=======================
<a href="#table-of-contents">back to top</a>

Contributions are most welcome! Use GitHub for issues and pull requests and we will happily help out in any way we can!

<a id="git-flow">Using GitFlow</a>
==================================

To contribute, simply submit a "Pull request" via GitHub.

We use GitFlow as a branching model and SemVer for versioning.

- When you submit a "Pull request" we require all changes to be squashed.
- We never merge more than one commit at a time. All the n commits on your feature branch must be squashed.
- We won't look at the pull request until Travis CI says the tests pass, make sure tests go well.

<a id="style-guidelines">Scala Style Guidelines</a>
===================================================

In spirit, we follow the [Twitter Scala Style Guidelines](http://twitter.github.io/effectivescala/).
We will reject your pull request if it doesn't meet code standards, but we'll happily give you a hand to get it right.

Some of the things that will make us seriously frown:

- Blocking when you don't have to. It just makes our eyes hurt when we see useless blocking.
- Testing should be thread safe and fully async, use ```ParallelTestExecution``` if you want to show off.
- Writing tests should use the pre-existing tools, they bring in EmbeddedCassandra, Zookeeper and other niceties, allowing us to run multi-datacenter tests.
- Use the common patterns you already see here, we've done a lot of work to make it easy.
- Don't randomly import stuff. We are very big on alphabetized clean imports.
- Tests must pass on both the Oracle and OpenJDK JVM implementations. The only sensitive bit is the Scala reflection mechanism used to detect columns.

YourKit Java Profiler
==================

![yourkit](https://s3-eu-west-1.amazonaws.com/websudos/oss/yklogo.png "YourKit Java Profiler")

We are very grateful to have the open source license support of YourKit, the most advanced Java profiler.

YourKit is the very core of our performance bottleneck testing, and without it phantom would still be a painfully slow tool.

[YourKit Java profiler](https://www.yourkit.com/java/profiler/index.jsp)
