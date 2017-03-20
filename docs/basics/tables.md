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
would "traverse" the type hierarchy and work out that the last thing in the chain
was the `recipes` object, and as a result it would use that as the name of the table within Cassandra.

However, the macro engine will instead try to figure out the last type in the type hierarchy that directly defines
columns.

This enables inheritance, but it does not support singletons/objects, so as a result, in an identical
scenario, the macro engine will infer the table name as "Recipes", based on the type information. If you hit trouble upgrading because names no longer match, simply
override the table name manually inside the table definition.

```scala
class MyDb(override val connector: CassandraConnection) extends Database[MyDb](connector) {
  object recipes extends Recipes with Connector {
    override def tableName: String = "recipes"
  }
}
```



<a id="data-modeling">Data modeling with phantom</a>
====================================================
<a href="#table-of-contents">back to top</a>

```scala
import com.outworkers.phantom.dsl._

case class ExampleModel (
  id: UUID,
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
```


<a id="extractors">Extractors and how they get generated</a>
====================================================
<a href="#table-of-contents">back to top</a>

As of phantom 2.0.0, phantom attempts to automatically derive an appropriate extractor for your record type. In plain
English, let's take the above example with `ExampleModel` and `ExampleRecord`. In the olden days, there was no automation,
so you would've had to manually tell phantom how to produce an `ExampleModel` from `com.datastax.driver.core.Row`.

It would look kind of like this:

```scala
  def fromRow(row: Row): ExampleModel = {
    new ExampleModel(
      id(row),
      name(row),
      props(row),
      timestamp(row),
      test(row)
    )
  }
```

Which is just boilerplate, because you already have the schema defined using the modelling DSL, so you don't
really want another layer of manual work. 

#### Extractor derivation and limitations

In 2.0.0, we have addressed this using macros, specifically a macro called `com.outworkers.phantom.macros.TableHelper`.
This class is automatically "computed" using the macro framework, at compile time, and invisibly summoned using implicit
macros. You don't really need to know the inner workings here all the time, as it's designed to be an invsible
background part of the toolkit, but sometimes you may run into trouble, as it's not perfect.

#### How the macro works

The macro is capable of matching columns based on their type, regardless of which order they are written in. So
even if your record types match the table column types in an arbitrary order, the macro can figure that out. It's just
like in the above example, where `ExampleModel` doesn't follow the same order as `ExampleRecord`.

So the algorithm for the match is very trivial and it works like this:

- First we extract a `Seq[(TermName -> Type)]` from both the record and the table. This basically lists
every field and their type, again for both table and record.

- We then build a type map of kind `ListMap[Type -> Seq[TermName]` for the table. This basically deals with problems
where we may have multiple term names of the same type, so in effect we do a `groupBy(_type)`.

- For every field type inside `Record`, we look for it in the type map and retrieve the column names
which could match that field type. If the names are identical, a direct match is found. We proceed
to the next step and we remove both the matched field and column from the dictionary ahead of the next lookup

- If a direct match is not found, **the next field column field matching the type is used**. In practice, this means
write order is respected, which coincides with the majority of use cases we have seen, but **can be wrong**. You can
either enable `debug` logging for `com.outworkers.phantom` or alternatively call `table.helper.showExtractor` and print
that to see a debug output of how phantom thinks your record maps to your table.


##### Field arity 

- The macro will successfully derive an extractor if your table contains all the types in your record and will fail if there
is at least one record type it couldn't find in a table. It looks for direct matches as well as existing implicit conversions
from A to B simultaneously. If your table has more fields than your record, the macro will also succeed, it will use only
those fields 
 

##### Limitations

The macro will attempt to match multiple fields of the same type based on their name. So if you have two `uuids` in both
your table and your record, they are matched perfectly if their names are identical. At this point in time, phantom 2.4.0,
no attempt is made to match fields using string distance algorithms, but we do anticipate this coming to a future version.
At this point in time, if a direct match for the field name is not found, then the "next" field in the order written
by the user is selected. Most of the time, this is the desired behaviour, but it can fail at runtime because it will mix up your
columns


<a id="store-methods">Automatically derived "store" method</a>
====================================================
<a href="#table-of-contents">back to top</a>

As of phantom 2.5.0, phantom will automatically infer the appropiate `store` method definition for a combination
of `Table` and `Record` type arguments. This means you no longer have to manually do anything at all and you can
just insert records ad-hoc using pre-generated things.

The inner workings of this generation are non trivial, but the TLDR is quite simple. Know how you
`extends CassandraTable[TableType, RecordType]`?`

- If the `TableType` has as many fields as your `RecordType`, the store method will take as an argument
an instance of the `RecordType`.

- If your `TableType` has more fields than the `RecordType`, your input looks like this: 
`def store(input: (Field1Type, Field2Type, ..., RecordType))`, so basically the columns that were found
in the table but not in the `RecordType` will be added to the front of the tuple in the order they were
 written.
