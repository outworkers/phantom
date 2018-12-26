[![Build Status](https://travis-ci.org/outworkers/phantom.svg?branch=develop)](https://travis-ci.org/outworkers/phantom?branch=develop) [![Coverage Status](https://coveralls.io/repos/github/outworkers/phantom/badge.svg?branch=develop)](https://coveralls.io/github/outworkers/phantom?branch=develop)  [![Codacy Rating](https://api.codacy.com/project/badge/grade/25bee222a7d142ff8151e6ceb39151b4)](https://www.codacy.com/app/flavian/phantom_2) [![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.outworkers/phantom-dsl_2.11/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.outworkers/phantom-dsl_2.11) [![Bintray](https://api.bintray.com/packages/outworkers/oss-releases/phantom-dsl/images/download.svg) ](https://bintray.com/outworkers/oss-releases/phantom-dsl/_latestVersion) [![ScalaDoc](http://javadoc-badge.appspot.com/com.outworkers/phantom-dsl_2.11.svg?label=scaladoc)](http://javadoc-badge.appspot.com/com.outworkers/phantom-dsl_2.11) [![Gitter](https://badges.gitter.im/Join%20Chat.svg)](https://gitter.im/outworkers/phantom?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)
======================================

First some basic setup, a database connection and a few tables to create examples on.

```tut:silent

import com.datastax.driver.core.SocketOptions
import com.outworkers.phantom.dsl._
import com.outworkers.util.samplers._

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
being using automated encodings available in phantom.

```tut:silent

import java.util.UUID
import org.joda.time.{ DateTime, DateTimeZone }

object PayloadExample {
  val customPayload = Payload(
    "timestamp" -> DateTime.now(DateTimeZone.UTC),
    "id" -> UUID.randomUUID()
  )
}

```

And this is how to use a payload inside a query of any kind, using the `withOptions` method.

```tut:silent

import scala.concurrent.Future
import com.outworkers.phantom.dsl._

trait UsingPayloads extends db.Connector {

    def storeWithPayload(metric: CarMetric, payload: Payload): Future[ResultSet] = {
      db.entries.store(metric).withOptions(_.outgoingPayload_=(payload)).future()
    }
}
```