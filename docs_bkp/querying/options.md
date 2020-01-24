| CI  | Test coverage(%) | Code quality | Stable version | ScalaDoc | Chat | Open issues | Average issue resolution time | 
| --- | ---------------- | -------------| -------------- | -------- | ---- | ----------- | ----------------------------- |
| [![Build Status](https://travis-ci.org/outworkers/phantom.svg?branch=develop)](https://travis-ci.org/outworkers/phantom?branch=develop) | [![Coverage Status](https://coveralls.io/repos/github/outworkers/phantom/badge.svg?branch=develop)](https://coveralls.io/github/outworkers/phantom?branch=develop) | [![Codacy Rating](https://api.codacy.com/project/badge/grade/25bee222a7d142ff8151e6ceb39151b4)](https://www.codacy.com/app/flavian/phantom_2) | [![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.outworkers/phantom-dsl_2.11/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.outworkers/phantom-dsl_2.11) | [![ScalaDoc](http://javadoc-badge.appspot.com/com.outworkers/phantom-dsl_2.11.svg?label=scaladoc)](http://javadoc-badge.appspot.com/com.outworkers/phantom-dsl_2.11) | [![Gitter](https://badges.gitter.im/Join%20Chat.svg)](https://gitter.im/outworkers/phantom?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge) | [![Percentage of issues still open](http://isitmaintained.com/badge/open/outworkers/phantom.svg)](http://isitmaintained.com/project/outworkers/phantom "%% of issues still open") | [![Average time to resolve an issue](http://isitmaintained.com/badge/resolution/outworkers/phantom.svg)](http://isitmaintained.com/project/outworkers/phantom "Average time to resolve an issue") |


First some basic setup, a database connection and a few tables to create examples on. The options described here
are generic for most Cassandra queries, and they are shared among CRUD queries, but disregarded on queries that don't require
them, such as `ALTER`.

```scala

import com.outworkers.phantom.dsl._
import com.outworkers.util.samplers._

object Connector {
  val default: CassandraConnection = ContactPoint.local
    .noHeartbeat().keySpace(
      KeySpace("phantom").ifNotExists().`with`(
        replication eqs SimpleStrategy.replication_factor(1)
      )
    )
}

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

class BasicDatabase(override val connector: CassandraConnection) extends Database[BasicDatabase](connector) {
  object entries extends AnalyticsEntries with Connector
}

object db extends BasicDatabase(Connector.default)


```


#### Using payloads

The full behaviour is described [here](https://docs.datastax.com/en/developer/java-driver/3.6/manual/custom_payloads/).
Cassandra makes it possible to implement a custom query handler which relies on custom incoming query payload to function.
You can pass additional data to queries using a `Payload`.


A `Payload` is a `map` of properties, where the keys are always strings and the values can be anything that can be encoded
on the Cassandra binary protocol, as a `java.nio.ByteBuffer`. There are several ways to construct a `Payload`, the easiest
being using automated encodings available in phantom, as described below.

Using the convenience `apply` method requires an implicit ProtocolVersion in scope, because the way the serialization is performed
depends on the version of the protocol, and it expects a series of tuples of the form `(String, Value)`, where the type of `Value`
has an implicit `Primitive` already defined, which will be used for serialization.

```scala

import java.util.UUID
import org.joda.time.{ DateTime, DateTimeZone }
import com.datastax.driver.core.ProtocolVersion
import com.outworkers.phantom.dsl._

trait PayloadExample extends db.Connector {

  // Phantom will automatically define this.
  // implicit val version: ProtocolVersion will come from inside the db.Connector.
  // It does however mean a Payload is directly tied to a given Cassandra version/protocol version.
  
  val customPayload = Payload(
    "timestamp" -> DateTime.now(DateTimeZone.UTC),
    "id" -> UUID.randomUUID(),
    "values" -> List(1, 2, 3, 4, 5)
  )
}

```

And this is how to use a payload inside a query of any kind, using the `withOptions` method.

```scala

import scala.concurrent.Future
import com.outworkers.phantom.dsl._

trait UsingPayloads extends db.Connector {

    def storeWithPayload(metric: CarMetric, payload: Payload): Future[ResultSet] = {
      db.entries.store(metric).withOptions(_.outgoingPayload_=(payload)).future()
    }
}
```
