### Phantom vs other Cassandra drivers


This document aims to offer you an insight into the available tooling
for Scala/Cassandra integrations and hopefully help you pick the right
tool for the job.

The available offering:

- [Datastax Java Driver](https://github.com/datastax/java-driver)
- [Phantom](https://github.com/outworkers/phantom)
- [Spark Cassandra Connector](https://github.com/datastax/spark-cassandra-connector)
- [Quill](https://github.com/getquill/quill)

### Feature comparison table

Let's first compare the basic qualities of these available drivers, looking
at a wider range of features and verticals, from how deeply integrated they
are with Cassandra and the support they offer, to their level of activity
and how up to date they are.


| Driver | Language | Commercial | Type-safe | Spark Support | Streams | DSL | Cassandra | Latest | Activity | Created |
| ------ | -------- | ----- | ------------------ | --------- | ------------- | ---------------- | --- | -------- | ---- | ----- | ----- |
| Datastax Java Driver | Java | yes | no | no | no | EDSL | 3.8.0 | 3.1.0 | High | 2012 |
| Phantom | Scala | yes | yes | no | yes | EDSL | 3.8.0 | 3.1.0 | High | 2013 |
| Spark Connector | Scala | yes | yes | yes | no | EDSL | 3.0 | High | 2014 |
| Quill | Scala | no | yes | no | yes | QDSL | 3.8.0 | 2015 |


### An overview of the various drivers and using them from Scala

#### Datastax Java Driver

Created by Datastax, the commercial company behind Cassandra, this is the underlying engine of all other drivers. Phantom, Quill and the Spark connector all use it underneath the hood to connect and execute queries. Phantom and Quill add a Scala friendly face to the driver, while the Spark connector does what it says on the tin, namely to focus on Cassandra - Spark integration.

So why not just use the Datastax driver for Scala? We would like to start by saying it's a really really good tool and some seriously good engineering lies behind it, but by sole virtue of being aimed at a Java audience, it does miss out a lot on some of the really powerful things you can do with the Scala compiler.

#### Cons of using the Java driver from Scala

- Writing queries is not type-safe, and the `QueryBuilder` is mutable.
- There is no DSL to define the tables, so dealing with them is still very much manual and string based. 
- Auto-generation of schema is not available, the CQL for tables or anything else must be manually created.
- The driver does not and cannot prevent you from doing bad things.
- You must constantly refer to the `session`.
- You constantly get back a `ResultSet`, which in most instances is not what you want.
- There is not native support for `scala.concurrent.Future` or any of the default Scala primitives.

Overall, if you wanted to, you could naturally use the Java driver inside a Scala application, but you probably wouldn't to. Again, this has very little to do with the quality of the driver itself as much as it has to do with the fact that the API is Java and therefore aimed at a Java consumer audience.

Libraries such as phantom and quill are designed to abstract away many of the internals of the Java driver and make it more appealing to a Scala audience, and phantom goes very very far to mask away all the underlying complexities and provide very advanced support for using Cassandra as a database layer.


#### Phantom

[![Build Status](https://travis-ci.org/outworkers/phantom.svg?branch=develop)](https://travis-ci.org/outworkers/phantom) [![Coverage Status](https://coveralls.io/repos/outworkers/phantom/badge.svg)](https://coveralls.io/r/outworkers/phantom)  [![Codacy Rating](https://api.codacy.com/project/badge/grade/25bee222a7d142ff8151e6ceb39151b4)](https://www.codacy.com/app/flavian/phantom_2) [![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.websudos/phantom-dsl_2.11/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.websudos/phantom-dsl_2.11) [![Bintray](https://api.bintray.com/packages/websudos/oss-releases/phantom-dsl/images/download.svg) ](https://bintray.com/websudos/oss-releases/phantom-dsl/_latestVersion) [![ScalaDoc](http://javadoc-badge.appspot.com/com.websudos/phantom-dsl_2.11.svg?label=scaladoc)](http://javadoc-badge.appspot.com/com.websudos/phantom-dsl_2.11) [![Gitter](https://badges.gitter.im/Join%20Chat.svg)](https://gitter.im/outworkers/phantom?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)

Phantom is designed to be a one stop destination. It is built exclusively to cater to the app level integration of Scala and Cassandra, meaning it is a lot more than a simple driver.

It is built on top of the Datastax Java driver, and uses all the default connection strategies and so on under the hood, without exposing any of the Java-esque APIs, but at the same time offering a multitude of features that are not available in the Java driver or in any other driver.

- A type safe Schema DSL that means you never have to deal with raw CQL again.
- A very advanced compile time mechanism that offers a fully type safe variant of CQL.
- A natural DSL that doesn't require any new terminology and aims to introduce a minimal learning curve. Phantom is not a leaking abstraction and it is exclusively built to target Cassnadra integration, therefore it has support for all the latest features of CQL and doesn't require constantly mapping terminology. Unlike LINQ style DSLs for instance, the naming will largely have 100% correspondence to CQL terminology you are already used to.
- Automated schema generation, automated table migrations, automated database generation and more, meaning you will never ever have to manually initialise CQL tables from scripts ever again.
- Native support of Scala concurrency primitives, from `scala.concurrent.Future` to more advanced access patterns such as reactive streams or even iteratees, available via separate dependencies.

#### Quill

Quill is a compile time macro based DSL that is capable of generating queries directly from a case class. It differs from phantom in several ways,
in the sense that Quill aims to build a leaking abstraction style QDSL, meaning a one size fits all driver for a number of different databases.

In the abstract sense it's the most like for like tool and probably the only one worth comparing to phantom, since the other variant
is pure Java and the Spark connector is obviously for, well, Spark. There are other drivers out there, but all discontinued or Thrift based,
some of which include Cassie from Twitter, Cascal, Astyanax from Netflix, and so on.

We would be the first to credit the engineering virtue behind it, it's an excellently designed tool and a very very powerful example of
just how far meta-programming can take you. Now that being said, there are a great number of items that make Quill less of suitable tool for application
layer Cassandra.

##### It introduces new terminology

One of the great perks of phantom is the fact that you don't really need to learn a new syntax. If you are familiar with Scala
and you are familiar wth SQL and CQl, you will feel right at home from the very first go, which means close to instant productivity.

It is true that phantom introduces other application level abstractions, such as the `Database` and the modelling DSL, all
of which you need to be aware, but there will never be a time where you read a phantom query and you wonder what the final result looks like.

##### It doesn't account for the CQL schema

Probably one of the most powerful features of phantom is the ability to be schema aware at compile time and the fact that you never
have to deal with manual CQL or to manually initialise schemas and various other bits. Gone are the days where you are
copy pasting CQL from one place to another in your build scripts or loading up schemas from `*.cql` files in `cqlsh`.

Phantom has a powerful mechanism to perform what we call schema auto-generation, which means with a single method call
it is capable of automatically initialising all the tables in your database on the fly against any keyspace of your choosing. It
can also account for more advanced indexing scenarios, user defined types and more, all on the fly.

Let's have a look at a basic example, for the basic `Recipe` case class, in this instance indexed by `url`.

```scala

import com.websudos.phantom.dsl._

case class Recipe(
  url: String,
  description: Option[String],
  ingredients: List[String],
  servings: Option[Int],
  lastCheckedAt: DateTime,
  props: Map[String, String],
  uid: UUID
)

class Recipes extends CassandraTable[ConcreteRecipes, Recipe] {

  object url extends StringColumn(this) with PartitionKey[String]

  object description extends OptionalStringColumn(this)

  object ingredients extends ListColumn[String](this)

  object servings extends OptionalIntColumn(this)

  object lastcheckedat extends DateTimeColumn(this)

  object props extends MapColumn[String, String](this)

  object uid extends UUIDColumn(this)


  override def fromRow(r: Row): Recipe = {
    Recipe(
      url(r),
      description(r),
      ingredients(r),
      servings(r),
      lastcheckedat(r),
      props(r),
      uid(r)
    )
  }
}
```

As of version 2.0.0, phantom is capable of auto-generating the `fromRow` method, so the mapping DSL is reduced to:

```
class Recipes extends CassandraTable[ConcreteRecipes, Recipe] {

  object url extends StringColumn(this) with PartitionKey[String]

  object description extends OptionalStringColumn(this)

  object ingredients extends ListColumn[String](this)

  object servings extends OptionalIntColumn(this)

  object lastcheckedat extends DateTimeColumn(this)

  object props extends MapColumn[String, String](this)

  object uid extends UUIDColumn(this)
}
```

It's definitely more boilerplate than Quill, there's no doubt about that, however, this simple DSL can help us to great
things:

- Control the name we want to use for our columns. Not the most interest feature,
but it helps avoid collissions with known Cassandra types. Currently this would be impossible in Quill.

- Generate the CQL schema on the fly. Every phantom table has a `.create` method, that will yield a `CreateQuery`,
where you can set the creation properties in minute details. The schema is then inferred from the DSL.

- Generate the entire database on the fly. By nesting tables in a `Database` object, we are able to implement
`autocreate`, `createAsync` and other convenience methods for automatically creating, truncating or dropping
an entire database in a single method call. Look ma', no manual CQL.

- Phantom is schema aware. Using an advanced implicit mechanism powered by the Shapeless library, phantom is
capable of "knowing" what queries are possible and what queries aren't. Let's take for example the `Recipes` above:

The following query is invalid, because we have not defined any index for the `uid` column.

```scala
database.recipes.select.where(_.uid eqs someid)
```

Quill will however happily compile and generate the query:


##### It doesn't account for protocol version/Cassandra version dependent behaviour

Numerous features or bugs are version dependent in Cassandra and the only way to provide the user with a consistent experience
across the numerous features is to be version aware, so in certain contexts the final query is only generated when the protocol
version of a give cluster is known.

Naturally this "knowing" can only happen at runtime, and it deals with very simple things such as set the consistency level of query. For
older protocol versions, the `CONSISTENCY LEVEL` part of a query was part of the `USING` clause, however in more recent
versions of the protocol the consistency level has to be specified per session. This is an optimisation to allow the nodes
to perform the necessary coordination to achieve the desired `CONSISTENCY LEVEL` without having to first parse
the query.

So CQL went from:

```sql
UPDATE keyspace.table WHERE ID = 'some_id' SET a = 'b' USING CONSISTENCY QUOROM;

// to

session.use(ConsistencyLevel.QUORUM)
UPDATE keyspace.table WHERE ID = 'some_id' SET a = 'b';
```

And any client library will need to transparently handle the change in CQL protocol details. Hoewever, this would
be impossible without knowing the version in advance, which means a cluster query which implies runtime.

