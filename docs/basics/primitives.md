## Primitives

 `Primitive` is a fundamental concept in phantom. Java uses the same terminology
to describe its set of native types. Expanding on the same terminology, `Primitives` are types Cassandra can natively understand.

The `Primitive` trait contains all the information to serialize and de-serialize a type to and back from Cassandra,
directly from a `java.nio.ByteBuffer`.


### Default primitives and columns

This is the list of predefined available columns with their corresponding Cassandra data types.
The columns are predefined for your convenience.

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
| ListColumn[Type]              | immutable.List[Type]      | list<type>        |
| SetColumn[Type]               | immutable.Set[Type]       | set<type>         |
| MapColumn[Type, Type]         | immutable.Map[Type, Type] | map<type, type>   |
| CounterColumn                 | scala.Long                | counter           |
| StaticColumn&lt;type&gt;      | &lt;type&gt;              | type static       |


<a id="optional-primitive-columns">Optional primitive columns</a>
===================================================================

Optional columns allow you to set a column to a ```null``` or a ```None```.
The outcome is that instead of a ```T``` you get an ```Option[T]``` and you can ```match, fold, flatMap, map``` on a ```None```.

The ```Optional``` part is only handled at a DSL level, it's not translated to Cassandra in any way.

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

It is also possible to express your entire table logic using just `Col` or `Column` if you. 
`Primitives` that do the real heavy lifting, which makes the examples below equivalent:

```scala

class Recipes extends Table[Recipes, Recipe] {

  object url extends StringColumn with PartitionKey

  object description extends OptionalStringColumn

  object ingredients extends ListColumn[String]

  object servings extends OptionalIntColumn

  object lastcheckedat extends DateTimeColumn

  object props extends MapColumn[String, String]

  object uid extends UUIDColumn
}
```

The same can be achieved by writing the below code:

```scala
class Recipes extends Table[Recipes, Recipe] {

  object url extends Col[String] with PartitionKey

  object description extends OptionalCol[String]

  object ingredients extends Col[List[String]]

  object servings extends OptionalCol[Int]

  object lastcheckedat extends Col[DateTime]

  object props extends Col[Map[String, String]]

  object uid extends Col[UUID]
}
```

Both of the above examples use the same macros under the hood which generate all Cassandra I/O required to marshall types across the wire to and from Cassandra.

### The mechanism that generates primitives

Primitive is a standard typeclass. `implicit` resolution is used to help Cassandra understand Scala types and "pretend" they are native. This feature enables support for a wide variety of types.

Several concepts have a very direct translation into Cassandra:

#### Tuples

A `scala.Tuple` can be directly mapped to a `Cassandra.Tuple`, which is a native Cassandra type.

Examples of such translations are visible below:

 | phantom columns                      | Java/Scala type                             | Cassandra type    |
 | ---------------                      |-------------------                          | ----------------- |
 | Col[(String, String)]                | Tuple2[String, String]                      | tuple<text, text> |
 | Col[(String, Int)]                   | Tuple2[String, Int]                         | tuple<text, int> |
 | Col[(String, Int, List[BigDecimal])] | Tuple3[String, Int, List[BigDecimal]]       | tuple<text, int, frozen<list<decimal>>> |

As seen in the third example, freezing of types should happen automatically during schema auto-generation, and
this is valid for any kind of type that requires freezing, including:

- Nested collection types.
- Primary collection or tuple types.
- UDT types in phantom pro.
- Auto-derived primitives in phantom-pro.

This also makes it possibly to derive arbitrary encodings for very complex types automatically.
This feature is only available in phantom-pro using the `autotables` module.

```scala
import com.outworkers.phantom.auto._

case class SubRecord(
  id: UUID,
  description: String,
  tags: Set[String],
  timestamp: DateTime
)

object SubRecord {
  implicit val subRecordPrimitive: Primitive[SubRecord] = Primitive.auto[SubRecord].derive
}
```

This would allow you to use `SubRecord` as a native type with all marshalling being at compile time with 0 runtime overhead.
`SubRecord` is now a valid column type.

```scala

case class Record(
  id: UUID,
  name: String,
  sub: SubRecord,
  timestamp: DateTime
)

abstract class MyTable extends Table[MyTable, Record] {
  object id extends UUIDColumn
  object name extends StringColumn
  object sub extends Col[SubRecord]
  object timestamp extends DateTimeColumn
}
```


#### Deriving new primitives from existing ones

This allows you to implement new primitives based on already available ones, often it's easier to leverage an existing implementation.

Let's assume you are trying to derive a primitive for `case class Test(value: String)`. Phantom allows this via
 `Primitive.derive` to produce a new implicit primitive for your custom type.

"Deriving" is simple, for a  a primitive of type `T` that already exists, we can define a new primitive
for any type `X` we want, provided there is a bijection from `T` to `X`, or in simple terms a way to convert a `T
 to an `X` and an `X` back to a `T` without losing any details in the process.

The implementation of `derive` uses context bound on `Source` to tell the compiler
the type `Source` should be an already defined primitive, and the two parameters are the conversion
functions from `Source` to `Target`. The output is a `Primitive[Target]`.

```
def derive[
  Target,
  Source : Primitive
](to: Target => Source)(from: Source => Target): Primitive[Target]
```

In practice, using `derive` for a simple scenario looks like this:

```scala

import com.outworkers.phantom.dsl._

case class Test(value: String)

object Test {
  implicit val testPrimitive: Primitive[Test] = Primitive.derive[Test, String](t => t.value)(Test.apply)
}
```

In essence, this is pretty straighforward, and now what I can do in any Cassandra table is this:

```scala

case class Wrapper(id: UUID, test: Test)

class MyTable extends CassandraTable[MyTable, Wrapper] {
  object id extends UUIDColumn with PartitionKey
  object test extends Col[Test](this)
}
```

So because of the implicit primitive, it is now safe and possible to use a `Test` instance everywhere in phantom,
including a `where` clause, an `insert`, a `set` or `update` clause, you name it.

### JDK8 Primitives

Phantom also natively supports some `java.time.*` JDK8 specific primitives as native types, though with a couple notable observations.

`OffsetDateTime` and `ZonedDateTime` are natively supported via the `phantom-jdk8` module, and all you have to do is `import com.outworkers.phantom.jdk8._`. This module is only compatible with Java 8 and requires an extra dependency as a result!

```scala
val phantomVersion = ".."

libraryDependencies ++= Seq(
  "com.outworkers" %% "phantom-jdk8" % phantomVersion
)
```

Primitives for JDK8 time come in two flavours, those who can remember their timezone
as part of the Cassandra marshalling and those which are automatically coerced
to UTC and willingly lose timezone specificity. You would use the latter when
you want to execute range queries based on these types.

When you don't want indexing, dates are encoded as `tuple<timestamp, timezone>` and
this is what allows to retrieve the timezone information back and have a bijective
primitive.

#### Using the compact `Table` DSL with JDK 8 columns
 
Note unlike other columns in the framework, the JDK8 columns will require you to pass in the `this` argument
even when you are using `Table`. This is a limitation of the Scala language itself, as we are not able
to add class members to another class via implicit augmentation.

That's why you should prefer to not use the now deprecated column aliases and instead rely on `Col` or `OptionalCol`.


##### Old DSL

```scala

case class Jdk8Row(
  pkey: String,
  offsetDateTime: OffsetDateTime,
  zonedDateTime: ZonedDateTime,
  localDate: LocalDate,
  localDateTime: LocalDateTime
)

abstract class PrimitivesJdk8 extends CassandraTable[PrimitivesJdk8, Jdk8Row] with RootConnector {

  object pkey extends StringColumn(this) with PartitionKey

  object offsetDateTime extends OffsetDateTimeColumn(this)

  object zonedDateTime extends ZonedDateTimeColumn(this)

  object localDate extends LocalDateColumn(this)

  object localDateTime extends LocalDateTimeColumn(this)
}
```

##### The new compact table DSL.

```scala
abstract class PrimitivesJdk8 extends Table[PrimitivesJdk8, Jdk8Row] {

  object pkey extends StringColumn with PartitionKey

  object offsetDateTime extends Col[OffsetDateTime]

  object zonedDateTime extends Col[ZonedDateTime]

  object localDate extends Col[LocalDate]

  object localDateTime extends Col[LocalDateTime]
}
```

#### Timezone preserving primitives

These will be by default available under `import com.outworkers.phantom.jdk8._`. The only
exception is `java.time.LocalDateTimeColumn` which is indexed with both imports.

| phantom columns               | Java/Scala type           | Cassandra type      |
| ---------------               |-------------------        | -----------------   |
| OffsetDateTimeColumn          | java.time.OffsetDateTime  | tuple<bigint, text> |
| ZonedDateTimeColumn           | java.time.ZonedDateTime   | tuple<bigint, text> |
| LocalDate                     | java.time.LocalDate       | localdate           |
| LocalDateTime                 | java.time.LocalDateTime   | timestamp           |

#### UTC indexed time primitives

These will be by default available under `import com.outworkers.phantom.jdk8.indexed._`

| phantom columns               | Java/Scala type           | Cassandra type      |
| ---------------               |-------------------        | -----------------   |
| OffsetDateTimeColumn          | java.time.OffsetDateTime  | timestamp           |
| ZonedDateTimeColumn           | java.time.ZonedDateTime   | timestamp           |
| LocalDate                     | java.time.LocalDate       | localdate           |
| LocalDateTime                 | java.time.LocalDateTime   | timestamp           |

