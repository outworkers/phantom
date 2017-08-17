### Select queries

This explores all the various `SELECT` functionality in CQL as described in the official reference [here](http://cassandra.apache.org/doc/latest/cql/dml.html#select).
Phantom aims to provide a 100% mapping with all the latest CQL features, but if you find anything that you need missing,
please help us by reporting it through GitHub issues.

#### Pre-requisites and setup.

To better explain the document to follow, it is easier if we reach common ground by refering to the same Cassandra table.
The below example is a simple Cassandra table with a more complex key that allows us to explore all the features of phantom
and demonstrate the available select API.

We will create a `AnalyticsEntries` table, where we hold information about a car's state over time for 2 properties we
care about, namely the `velocity` and `tirePressure`. We leverage the `TimeUUID` Cassandra type to store information
about timestamps, order the logs we receive in descending order(most recent record first), and prevent any colissions.

If we would just use a timestamp type, if we were to receive two logs for the same car at the exact same timestamp,
the entries would override each other in Cassandra, because in effect they would have the same partition key
and the same clustering key, so the whole primary key would be identical.

```scala

import com.outworkers.phantom.dsl._

case class CarMetric(
  car: UUID,
  id: UUID,
  velocity: Double,
  tirePressure: Double
)

abstract class AnalyticsEntries extends Table[AnalyticsEntries, CarMetric] {
  object car extends UUIDColumn with PartitionKey
  object id extends TimeUUIDColumn with ClusteringOrder with Descending
  object velocity extends DoubleColumn
  object tirePressure extends DoubleColumn
}

```

#### Available query methods.

The following is the list of available query methods on a select, and it can be used to leverage standard `SELECT` functionality
 in various ways.
 
 
| Method name                   | Return type                         | Purpose                                                |
| ----------------------------- | ----------------------------------- | -----------------------------------------------------  |
| `future`                      | `com.ouwotkers.phantom.ResultSet`   | Available on all queries, returns the raw result type. |
| `one`                         | `Option[R]`                         | Select a single result as an `Option[R]`               |
| `fetch`                       | `List[R]`                           | Select a small list of records without a paging state  |
| `fetch(modifier)`             | `List[R]`                           | Select a small list of records without a paging state  |
| `fetchRecord`                 | `ListResult[R]`                     | Fetch a small result together with the `ResultSet`     |
| `paginateRecord`              | `ListResult[R]`                     | Fetch a paginated result together with the `ResultSet` and `PagingState` |
| `paginateRecord(modifier)`    | `ListResult[R]`                     | Fetch a paginated result together with the `ResultSet` and `PagingState` |
| `paginateRecord(pagingState)` | `ListResult[R]`                     | Fetch a paginated result together with the `ResultSet` and `PagingState` |


#### Paginating results by leveraging paging states and automated Cassandra pagination.

There are situations where you can not retrieve a whole list of results in a single go, and for that reason
Cassandra offers paging states and automated pagination. Phantom makes that functionality available through a set of overloaded
methods called `paginateRecord`.

As opposed to a normal `one` or `fetch` query, calling `paginateRecord` will return a `ListResult`, that allows
you to look inside the original `ResultSet`, as well as the `PagingState`. The state can then be serialized
to a string, and using that string is the key to pagination from a client.


####  Aggregation functions

Cassandra supports a set of native aggregation functions. To explore them in more detail, have a look
at [this tutorial](http://christopher-batey.blogspot.co.uk/2015/05/cassandra-aggregates-min-max-avg-group.html).

It's important to note aggregation functions rely on `scala.Numeric`. We use this to transparently
handle multiple numeric types as possible returns. Phantom supports the following aggregation operators.

The `T` below means the return type will depend on the type of the column you call the operator on.
The average of a `Float` column will come back as `scala.Float` and so on.


| Scala operator     | Cassandra operator   | Return type           |
| --------------     | -------------------- | --------------------- |
| `sum[T : Numeric]` | SUM                  | `Option[T : Numeric]` |
| `min[T : Numeric]` | MIN                  | `Option[T : Numeric]` |
| `max[T : Numeric]` | MAX                  | `Option[T : Numeric]` |
| `avg[T : Numeric]` | AVG                  | `Option[T : Numeric]` |
| `count`            | COUNT                | `Option[scala.Long]`  |

To take advantage of these operators, simply use the default import, combined with the `function` argument
and the `aggregate` function. A few examples are found in [SelectFunctionsTesting.scala](/phantom-dsl/src/test/scala/com/outworkers/phantom/builder/query/db/specialized/SelectFunctionsTesting.scala#L99).

The structure of an aggregation query is simple, and the rturn type is 

```scala
database.primitives.select.function(t => sum(t.long)).where(_.pkey eqs record.pkey).aggregate()
database.primitives.select.function(t => min(t.int)).where(_.pkey eqs record.pkey).aggregate()
database.primitives.select.function(t => avg(t.int)).where(_.pkey eqs record.pkey).aggregate()
```

