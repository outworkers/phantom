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

abstract class AnalyticsEntries extends CassandraTable[AnalyticsEntries, CarMetric] with RootConnector {
  object car extends UUIDColumn with PartitionKey
  object id extends TimeUUIDColumn with ClusteringOrder with Descending
  object velocity extends DoubleColumn
  object tirePressure extends DoubleColumn
}

```

#### Available query methods.

The following is the list of available query methods on a select, and it can be used to leverage standard `SELECT` functionality
 in various ways.
 
 
| Method name | Return type | Purpose |
| =========== | =========== | ======= |



#####



#### Paginating results by leveraging paging states and automated Cassandra pagination.

There are situations where you can not retrieve a whole list of results in a single go, and for that reason
Cassandra offers paging states and automated pagination. Phantom makes that functionality available through a set of overloaded
methods called `paginateRecord`.