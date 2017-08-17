### Execution backends

Internally, Cassandra queries are handled by the Datastax driver which uses the underlying Guava lib to send queries to Cassandra.
In phantom it is however possible to consume these results with different concurrency backends.

Several are supported, some require additional modules to be added as deps:

- Scala Concurrency, available by default. Queries return `scala.concurrent.Future`.
- Reactive Streams, requires a dependency on `phantom-streams`.
- Play Iteratees, again available via `phantom-streams`.
- Twitter Util Concurrency, requires a dependency on `phantom-finagle`.
- Twitter Spools, similar to Iteratees, requires `phantom-finagle`.


All the execution backends are optimised for different purposes. Scala and Twitter concurrency in non
streaming fashion are designed for "every day usage", while streaming is for larger scale or higher volume
operations inside your application code. A sweet spot in between the every day and full blown Apache Spark.


### Breaking changes

In the past, various implementation backends used to require separate method names. E.g `fetch` would give you back a 
`scala.concurrent.Future` while `collect` would do the same thing but give you back `com.twitter.util.Future`.

These methods were:

- `execute`, equivalent to `.future()` but producing a Twitter Future.
- `get`, equivalent to `.one()` but producing a Twitter Future.
- `collect`, equivalent to `.fetch()` but producing a Twitter Future.
- `aggregated`, equivalent to `.aggregate()` but producing a Twitter Future.

All these methods have now been removed from the framework completely, and instead you can simply import
a different execution backend and the right queries will be produced.


This became increasingly problematic to manage because we want to be able to consistently and transparently support
any number of execution backends without having to invent new adjacent method names at every turn.

As of phantom `2.14.0`, that has now changed, and the execution semantics are completely de-coupled from
query generation. As a consequence, all the methods that used to exist to handle Twitter Futures no longer exist.


Instead of having to import both packages:

```scala
import com.outworkers.phantom.dsl._
import com.outworkers.phantom.finagle._
```

You are now required to choose, and import only one of the packages in scope, as they now define conflicting
execution specific implicits. Simply put, the same method names will return different kinds of futures
depending on your imported execution backend.

The below code will produce a `scala.concurrent.Future`:

```scala
import com.outworkers.phantom.dsl._


val f: scala.concurrent.Future[Option[?]] = db.table.select.where(_.id eqs id).one()
```


If you instead import from the `finagle` package, the same query will produce a Twitter future:

```scala
import com.outworkers.phantom.finagle._


val f: com.twitter.util.Future[Option[?]] = db.table.select.where(_.id eqs id).one()
```

This is now handled invisibly, and it is true for all the query methods.