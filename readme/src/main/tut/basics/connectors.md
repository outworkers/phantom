phantom
[![Build Status](https://travis-ci.org/outworkers/phantom.svg?branch=develop)](https://travis-ci.org/outworkers/phantom?branch=develop) [![Coverage Status](https://coveralls.io/repos/github/outworkers/phantom/badge.svg?branch=develop)](https://coveralls.io/github/outworkers/phantom?branch=develop)  [![Codacy Rating](https://api.codacy.com/project/badge/grade/25bee222a7d142ff8151e6ceb39151b4)](https://www.codacy.com/app/flavian/phantom_2) [![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.outworkers/phantom-dsl_2.11/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.outworkers/phantom-dsl_2.11) [![Bintray](https://api.bintray.com/packages/outworkers/oss-releases/phantom-dsl/images/download.svg) ](https://bintray.com/outworkers/oss-releases/phantom-dsl/_latestVersion) [![ScalaDoc](http://javadoc-badge.appspot.com/com.outworkers/phantom-dsl_2.11.svg?label=scaladoc)](http://javadoc-badge.appspot.com/com.outworkers/phantom-dsl_2.11) [![Gitter](https://badges.gitter.im/Join%20Chat.svg)](https://gitter.im/outworkers/phantom?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)
===============================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================


Connectors are a thin abstraction layer around a construct native to the Datastax Java Driver, and provide a way to implicitly "inject" a `Session`
where it is required. On top of that, they also allow specifying all the settings you might care about when connecting to Cassandra, such as heartbeat intervals,
pooling options and so on.


The options available are better described on the official [ClusterBuilder documentation](https://docs.datastax.com/en/drivers/java/3.1/com/datastax/driver/core/Cluster.Builder.html), but
the same will be available in phantom, and the `ContactPoint` implementation will simply leverage the `ClusterBuilder` API under the hood.


An example of how to build a connection is found below, and uses `ContactPoint.local`, which is just a convenience method that's meant to
use a connection to `localhost` on port `9042`, the standard CQL port. There's also `ContactPoint.embedded`, which works together with
the  SBT cassandra plugins we offer, `phantom-sbt` and `phantom-docker`, and will attempt to connect to `localhost:9142`.

```tut:silent

import com.datastax.driver.core.SocketOptions
import com.outworkers.phantom.dsl._

object ConnectorExample {

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

To take advantage of the native Java Driver options, simply use the `withClusterBuilder` method. In the example below,
we are going use password protected authentication to Cassandra, using `PlainTextAuthProvider`. Hopefully it is
easy to see how you would 

```tut:silent

import com.datastax.driver.core.{PlainTextAuthProvider, SocketOptions}
import com.outworkers.phantom.dsl._

object ConnectorExample2 {

  val default: CassandraConnection = ContactPoint.local
    .withClusterBuilder(
      _.withSocketOptions(
        new SocketOptions()
          .setConnectTimeoutMillis(20000)
          .setReadTimeoutMillis(20000)
      ).withAuthProvider(
        new PlainTextAuthProvider("username", "password")
      )
  ).keySpace(
    KeySpace("phantom").ifNotExists().`with`(
      replication eqs SimpleStrategy.replication_factor(1)
    )
  )
}

```


### Keyspace options

There is a second set of options you can control via the `ContactPoint`, and these relate to the CQL query used to create
the keyspace. Not everyone chooses to initialise Cassandra keyspaces with phantom, but it's a useful bit of kit to have
for your development environment.

Your keyspace creation query is passed through to initialised session, and together with phantom's database automated
creation functionality, you can use phantom to initialise both keyspaces and tables inside it on the fly, not to mention
indexes and UDT types(phantom pro only).

 
To build a keyspace query, use `with` and `and` to chain options on the query. It's important to note they require
a special assigning operator, namely `eqs` instead of `=`, just like phantom queries. This is to prevent a potentially
confusing overload of standard operators.

*Note*: Using the `with` operator required backticks ``` `with` ```, as the keyword `with` is also a native Scala keyword
used for trait mixins. We preserve the name to match the CQL syntax, but if you would like to avoid that, use `option` instead.

The below examples will produce the same output CQL query.

#### Using the ```scala `with` ``` keyword

```tut:silent

object KeySpaceQueryWith {
  val query = KeySpace("phantom").ifNotExists()
    .`with`(replication eqs SimpleStrategy.replication_factor(1))
    .and(durable_writes eqs true)
}

```

#### Using the ```scala option ``` keyword

```tut:silent

object KeySpaceQueryOption {
  val query = KeySpace("phantom").ifNotExists()
    .otion(replication eqs SimpleStrategy.replication_factor(1))
    .and(durable_writes eqs true)
}

```


#### Keyspace configuration options

Using the DSL, you can configure three main things:

- Replication strategy
- Topology strategy
- Durable writes(true/false)

More advanced options are also supported, such as `NetworkTopologyStrategy`.

```tut:silent

object NetworkTopologyExample {

    val query = KeySpace("phantom").ifNotExists()
      .`with`(replication eqs NetworkTopologyStrategy
        .data_center("data1", 2)
        .data_center("data2", 3)
      ).and(durable_writes eqs true)
}
```
