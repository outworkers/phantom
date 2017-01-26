### How Cassandra tables are implemented in Phantom

#### 2.0.0 changed the way the table name is inferred.

Back in the pre 2.0.0 days where reflection was king, the reflection mechanism trivially allowed
the final piece of the inheritance chain of a particular table to dictate its name. That means
the usual way in which a table name used to get set was by the `object` that was always part
of a `Database` class.

Let's consider the below example.

```scala

import com.outworkers.phantom.dsl._
import org.joda.time.DateTime

case class Recipe(
  url: String,
  description: Option[String],
  ingredients: List[String],
  servings: Option[Int],
  lastCheckedAt: DateTime,
  props: Map[String, String],
  uid: UUID
)

abstract class Recipes extends CassandraTable[Recipes, Recipe] with RootConnector {

  object url extends StringColumn(this) with PartitionKey

  object description extends OptionalStringColumn(this)

  object ingredients extends ListColumn[String](this)

  object servings extends OptionalIntColumn(this)

  object lastcheckedat extends DateTimeColumn(this)

  object props extends MapColumn[String, String](this)

  object uid extends UUIDColumn(this)
}

class MyDb(override val connector: CassandraConnection) extends Database[MyDb](connector) {
  object recipes extends Recipes with Connector
}
```

In the past, when the table was nested within a `Database`, such as as above, the reflection mechanism
would essentially "traverse" the entire type hierarchy and work out that the last thing in the chain
was the `recipes` object, and as a result it would use that as the name of the table within Cassandra.

However, the macro based engine is not capable of replicating that functionality, because unlike the reflection mechanism
it does not have a way to know how many types you will be extending the type of the table. As a result,
the macro engine will instead try to figure out the last type in the type hierarchy that directly defines
columns.

This is done to support inheritance, but it does not support singletons and objects, so as a result, in an identical
scenario, the macro engine will infer the table name as "Recipes", based on the type information. This is usually
not a problem in any way, but if you hit trouble upgrading because names no longer match, it is worth understanding
where such pains originate.


<a id="data-modeling">Data modeling with phantom</a>
====================================================
<a href="#table-of-contents">back to top</a>

```scala

import java.util.Date
import com.outworkers.phantom.dsl._

case class ExampleModel (
  id: Int,
  name: String,
  props: Map[String, String],
  timestamp: Int,
  test: Option[Int]
)

abstract class ExampleRecord extends CassandraTable[ExampleRecord, ExampleModel] with RootConnector {
  object id extends UUIDColumn(this) with PartitionKey
  object timestamp extends DateTimeColumn(this) with ClusteringOrder with Ascending
  object name extends StringColumn(this)
  object props extends MapColumn[ExampleRecord, ExampleModel, String, String](this)
  object test extends OptionalIntColumn(this)
}

```