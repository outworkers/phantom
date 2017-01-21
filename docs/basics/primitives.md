phantom
[![Build Status](https://travis-ci.org/outworkers/phantom.svg?branch=develop)](https://travis-ci.org/outworkers/phantom?branch=develop) [![Coverage Status](https://coveralls.io/repos/outworkers/phantom/badge.svg)](https://coveralls.io/r/outworkers/phantom)  [![Codacy Rating](https://api.codacy.com/project/badge/grade/25bee222a7d142ff8151e6ceb39151b4)](https://www.codacy.com/app/flavian/phantom_2) [![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.outworkers/phantom-dsl_2.11/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.outworkers/phantom-dsl_2.11) [![Bintray](https://api.bintray.com/packages/outworkers/oss-releases/phantom-dsl/images/download.svg) ](https://bintray.com/outworkers/oss-releases/phantom-dsl/_latestVersion) [![ScalaDoc](http://javadoc-badge.appspot.com/com.outworkers/phantom-dsl_2.11.svg?label=scaladoc)](http://javadoc-badge.appspot.com/com.outworkers/phantom-dsl_2.11) [![Gitter](https://badges.gitter.im/Join%20Chat.svg)](https://gitter.im/outworkers/phantom?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)
===============================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================

## Primitives

One of the most important constructs in phantom is the `Primitive` type. Much like Java uses the same terminology
to describe the set of types that are native to its platform, we leverage and overload the term to refer to a set of types
that Cassandra can natively understand.

The `Primitive` trait has a simplistic implementation that contains all the necessary information to
basically serialize and de-serialize a given type as it is retrieved from the database, and that's the 
basic primary goal. Whether it's a normal column or a user defined type or anything similar, this is
the construct that de-serializes everything into familiar Scala types.


### The mechanism that generates primitives

The physical implementation is based on the typeclass model, a very popular Scala concept, and phantom
leverages implicit resolution to help Cassandra understand more of your Scala types. And there are a number of language
features that in conjunction with appropriate Cassandra types we can leverage to produce seemingly native support
for a wide variety of types.

Several concepts have very direct translation into Cassandra:


#### Tuples

A `scala.Tuple` can be directly mapped to a `Cassandra.Tuple`. The tuple concept excepts natively within Cassandra and
 we can therefore produce directly translatable mappings between a Scala type and a Cassandra type.
 
 
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
  object id extends UUIDColumn(this) with PartitionKey
  object test extends Col[Test](this)
}
```

So because of the implicit primitive, it is now safe and possible to use a `Test` instance everywhere in phantom,
including a `where` clause, an `insert`, a `set` or `update` clause, you name it.