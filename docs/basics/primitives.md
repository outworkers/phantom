## Primitives

One of the most important constructs in phantom is the `Primitive` type. Much like Java uses the same terminology
to describe the set of types that are native to its platform, we leverage and overload the term to refer to a set of types
that Cassandra can natively understand.

The `Primitive` trait has a simplistic implementation that contains all the necessary information to
basically serialize and de-serialize a given type as it is retrieved from the database, and that's the
basic primary goal. Whether it's a normal column or a user defined type or anything similar, this is
the construct that de-serializes everything into familiar Scala types.


### Default primitives and columns

This is the list of available columns and how they map to C* data types. The columns
here are nothing more than simple helpers and they do not actually contain any
specialized implementation. That means it is the primitives doing all the work,
columns are just a nice way for you to write DSL code that's readable.

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
| CounterColumn                 | scala.Long                | counter           |
| StaticColumn&lt;type&gt;      | &lt;type&gt;              | type static       |


<a id="optional-primitive-columns">Optional primitive columns</a>
===================================================================

Optional columns allow you to set a column to a ```null``` or a ```None```.
Use them when you really want something to be optional.
The outcome is that instead of a ```T``` you get an ```Option[T]``` and you can ```match, fold, flatMap, map``` on a ```None```.

The ```Optional``` part is handled at a DSL level, it's not translated to Cassandra in any way.

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

It is also possible to express your entire table logic using just `Col` if you
prefer it. Because it is `Primitives` that do the real heavy lifting, the below
are equivalent:

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

  object description extends Col[Option[String]]

  object ingredients extends Col[List[String]]

  object servings extends Col[Option[Int]]

  object lastcheckedat extends Col[DateTime]

  object props extends Col[Map[String, String]]

  object uid extends Col[UUID]
}
```

There is virtually no difference whatsoever in doing things one way or the other,
the encodings and serializers/deserializers are handled by the same macro, which
will do its magic invisibly in the background and generate all the Cassandra I/O
code required to marshall those types back and forth.

### The mechanism that generates primitives

The physical implementation is based on the typeclass model, a very popular Scala concept, and phantom
leverages implicit resolution to help Cassandra understand more of your Scala types. And there are a number of language
features that in conjunction with appropriate Cassandra types we can leverage to produce seemingly native support
for a wide variety of types.

Several concepts have very direct translation into Cassandra:


#### Tuples

A `scala.Tuple` can be directly mapped to a `Cassandra.Tuple`. The tuple concept excepts natively within Cassandra and
 we can therefore produce directly translatable mappings between a Scala type and a Cassandra type.

Examples of such translations are visible below:

 | phantom columns                | Java/Scala type           | Cassandra type    |
 | ---------------                |-------------------        | ----------------- |
 | Col[(String, String)]          | Tuple2[String, String]    | tuple<text, text> |
 | Col[(String, Int)]             | Tuple2[String, Int]       | tuple<text, int> |
 | Col[(String, Int, List[BigDecimal])] | Tuple3[String, Int, List[BigDecimal]]       | tuple<text, int, frozen<list<decimal>>> |

As seen in the third example, freezing of types should happen automatically during schema auto-generation, and
this is valid for any kind of type that requires freezing, including:

- Nested collection types.
- Primary collection or tuple types.
- UDT types in phantom pro.

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

This would allow you to use `SubRecord` transparently as a natively Cassandra type
and all necessary marshalling is generated at compile time with 0 runtime overhead using
advanced macro machinery and you could arbitrarily use `SubRecord` as a valid column type.

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

Ok, so phantom comes with a pre-defined set that helps with the most common scenarios that are well known and
loved in Cassandra. But what if you want to roll your own type? Well, you can obviously choose to fully implement
the primitive trait though in some scenarios it's often just easier to simply leverage an existing implementation.

For the sake of argument, let's assume you are trying to derive a primitive for `case class Test(value: String)`. Phantom makes
this reasonably simply, all you have to do is to use `Primitive.derive` to produce a new implicit primitive in scope
that helps educate Phantom and simultaneously Cassandra of what your type `means`.

"Deriving" is very simple, if we have a primitive for a type `T` that already exists, then we can define a new primitive
for any type `X` we want, provided there is a bijection from `T` to `X`, or in simple terms a way to convert a `T
 to an `X` and an `X` back to a `T` without losing any details in the process.

The signature of `derive` looks like this. We use a context bound on `Source` to tell the compiler
we expect type `Source` to have a pre-defined primitive, and the two parameters are the conversion
functions. The output is a `Primitive[Target]`, as anticipated.

```
def derive[
  Target,
  Source : Primitive
](to: Target => Source)(from: Source => Target): Primitive[Target]
```

In practice, using `derive` for a simple scenario looks like the below. In this example the implicit primitive
is defined in the companion object of a class, and this is only because in Scala the compiler "knows" to look
here for any implicit for type `Test`. This is a default characteristic of the implicit resolution mechanism
in the language itself, so we are not doing anything special per say other than leveraging Scala features.

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

Phantom also natively supports some `java.time.*` JDK8 specific primitives as native types, though with a couple notable observations. `OffsetDateTime` and `ZonedDateTime` are natively supported via `"com.outworkers" %% "phantom-jdk8" % version`, and all you have to do is `import com.outworkers.phantom.jdk8._`.
