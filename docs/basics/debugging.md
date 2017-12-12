# Debugging

This section is about the various debug options in phantom, logging, enabling special compilation flags and other such options
that allow you to see what's going on behind the scenes. Most of these options do not apply for earlier, pre 2.0.0 versions of phantom.  


## How to enable logging in phantom

Phantom does not configure a specific logging backend for you, it relies on SLF4J to allow you to choose your own. All
queries are logged through one central logger, namely `com.outworkers.phantom`, and we also create one logger instance
per Cassandra table, to allow you to track queries belonging to a particular table more easily. 


It's worth noting what the most verbose loggers will be in this setup, as you will need to pay attention to them:

- `com.outworkers.phantom`, the phantom specific query logger.
- `com.datastax.driver.core`, the logger for the Datastax Java Driver.
- `io.netty`, the logger for the underlying Netty async client, which manages the low level transports used to talk to Cassandra.

These packages produces the most interesting and verbose part of what you will get using phantom, so it's worth paying
particular attention here and configuring them properly to suit your needs.


### Example: Configuring phantom with Log4J logging.

In the below example, we are configuring Apache Log4J logging to work with Cassandra Unit. This is useful when you
write tests against `phantom-sbt` for instance.

```xml
<?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE log4j:configuration SYSTEM "log4j.dtd">
<log4j:configuration xmlns:log4j="http://jakarta.apache.org/log4j/">
    <appender name="outputConsole" class="org.apache.log4j.ConsoleAppender">
        <param name="Target" value="System.out" />
        <layout class="org.apache.log4j.PatternLayout">
            <param name="ConversionPattern" value="%d [%t] %-5p %c{3} - %m%n" />
        </layout>
    </appender>

    <logger name="org.cassandraunit">
        <level value="debug" />
    </logger>
    <logger name="org.apache.cassandra">
        <level value="error" />
    </logger>

    <logger name="com.datastax.driver.core">
        <level value="error" />
    </logger>

    <logger name="jetty">
        <level value="error" />
    </logger>

    <logger name="com.outworkers.phantom">
        <level value="error"/>
    </logger>

    <logger name="me.prettyprint">
        <level value="error" />
    </logger>

    <root>
        <priority value="error" />
        <appender-ref ref="outputConsole" />
    </root>

</log4j:configuration>
```

### Example: Configuring phantom with Logback

This is a very simple example using the more popular Logback framework


```xml
<configuration debug="true">

    <appender name="STDOUT" class="ch.qos.logback.core.ConsoleAppender">
        <!-- encoders are assigned the type
             ch.qos.logback.classic.encoder.PatternLayoutEncoder by default -->
        <encoder>
            <pattern>%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n</pattern>
        </encoder>
    </appender>

    <root level="warning">
        <appender-ref ref="STDOUT" />
    </root>

    <logger name="com.outworkers.phantom" level="ERROR"/>
    <logger name="com.datastax.driver.core" level="ERROR"/>
    <logger name="io.netty" level="ERROR"/>

</configuration>
```


## Compilation level special log flags

Phantom relies quite heavily on Scala macros to work, which means a lot of logic happens hidden away in compilation time.
Because the macro API is not always consistent, some errors are "masked" and incorrectly hidden by the compiler. This can
prove difficult to debug, which is why we've created a collection of macro debug flags. 

If you are entirely new to macros, it's worth understanding they are effectively used to generate code, for instance
automated JSON formats solely based on the structure of a case class, like `play-json` does with its `Json.format[CaseClass]` syntax.

Phantom uses macros to extract table descriptors, a list of names and types for each Cassandra column, and match that against the case class input to `com.outworkers.phantom.CassandraTable`,
to automatically infer the `fromRow` extractor. Without macros, you would need to do this manually, just like in previous versions of phantom.

A lot of the macros are used to "compute" or template implicits on the fly, which means we programmatically determine
the structure of various implicits entirely using macros, generating actual code behind the scenes. 

*Note*: To use any of these flags, you will need to import them in the right place. Unlike traditional compilation flags,
these flags are not global, they are just normal implicits. You need to make sure they are imported in all the scopes where
you want to get logging information, otherwise the macros will not find the implicit triggers and will skip printing
debug information.

All flags are available under the following object import:

```scala
com.outworkers.phantom.macros.debug.Options
```

### `com.outworkers.phantom.macros.debug.Options.ShowTrees` flag

Let's assume the following schema DSL. All you have to do is to import the implicit at the top level, into the
same file as the table is defined. If you define more than one table per file, you will see information printed
out for every single tree.

```scala

import com.outworkers.phantom.dsl._
import com.outworkers.phantom.macros.debug.Options.ShowTrees
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

abstract class Recipes extends Table[Recipes, Recipe] {

  object url extends StringColumn with PartitionKey
 
   object description extends OptionalStringColumn
 
   object ingredients extends ListColumn[String]
 
   object servings extends OptionalIntColumn
 
   object lastcheckedat extends DateTimeColumn
 
   object props extends MapColumn[String, String]
 
   object uid extends UUIDColumn
 }
 
 class MyDb(override val connector: CassandraConnection) extends Database[MyDb](connector) {
   object recipes extends Recipes with Connector
 }
```


The debug output in the console looks like this. It's not easily readable, but it does show store type computed by the macro
is ```Recipe```

```scala
{
    import shapeless.::
    final class anon$macro$1 extends _root_.com.outworkers.phantom.macros.TableHelper[Recipes, Recipe] {
      type Repr = Recipe :: shapeless.HNil;
      def tableName: _root_.java.lang.String = com.outworkers.phantom.NamingStrategy.identityStrategy.inferName("recipes");
      def store(table: Recipes, input: Recipe :: shapeless.HNil)(implicit space: com.outworkers.phantom.connectors.KeySpace): _root_.com.outworkers.phantom.builder.query.InsertQuery.Default[Recipes, Recipe] = table.insert.values(_root_.com.outworkers.phantom.builder.query.engine.CQLQuery(table.url.name).->(_root_.com.outworkers.phantom.builder.query.engine.CQLQuery(table.url.asCql(input.apply(_root_.shapeless.Nat._0).url))), _root_.com.outworkers.phantom.builder.query.engine.CQLQuery(table.description.name).->(_root_.com.outworkers.phantom.builder.query.engine.CQLQuery(table.description.asCql(input.apply(_root_.shapeless.Nat._0).description))), _root_.com.outworkers.phantom.builder.query.engine.CQLQuery(table.ingredients.name).->(_root_.com.outworkers.phantom.builder.query.engine.CQLQuery(table.ingredients.asCql(input.apply(_root_.shapeless.Nat._0).ingredients))), _root_.com.outworkers.phantom.builder.query.engine.CQLQuery(table.servings.name).->(_root_.com.outworkers.phantom.builder.query.engine.CQLQuery(table.servings.asCql(input.apply(_root_.shapeless.Nat._0).servings))), _root_.com.outworkers.phantom.builder.query.engine.CQLQuery(table.lastcheckedat.name).->(_root_.com.outworkers.phantom.builder.query.engine.CQLQuery(table.lastcheckedat.asCql(input.apply(_root_.shapeless.Nat._0).lastCheckedAt))), _root_.com.outworkers.phantom.builder.query.engine.CQLQuery(table.props.name).->(_root_.com.outworkers.phantom.builder.query.engine.CQLQuery(table.props.asCql(input.apply(_root_.shapeless.Nat._0).props))), _root_.com.outworkers.phantom.builder.query.engine.CQLQuery(table.uid.name).->(_root_.com.outworkers.phantom.builder.query.engine.CQLQuery(table.uid.asCql(input.apply(_root_.shapeless.Nat._0).uid))));
      def tableKey(table: Recipes): _root_.java.lang.String = _root_.com.outworkers.phantom.builder.QueryBuilder.Create.primaryKey(_root_.scala.collection.immutable.List[com.outworkers.phantom.column.AbstractColumn[_]](table.url).map(((x$2) => x$2.name)), _root_.scala.collection.immutable.List[com.outworkers.phantom.column.AbstractColumn[_]]().map(((x$3) => x$3.name))).queryString;
      def fromRow(table: Recipes, row: _root_.com.outworkers.phantom.Row): Recipe = new Recipe(table.url.apply(row), table.description.apply(row), table.ingredients.apply(row), table.servings.apply(row), table.lastcheckedat.apply(row), table.props.apply(row), table.uid.apply(row));
      def fields(table: Recipes): scala.collection.immutable.Seq[com.outworkers.phantom.column.AbstractColumn[_]] = scala.collection.immutable.Seq.apply[com.outworkers.phantom.column.AbstractColumn[_]](table.instance.url, table.instance.description, table.instance.ingredients, table.instance.servings, table.instance.lastcheckedat, table.instance.props, table.instance.uid);
      def sasiIndexes(table: Recipes): scala.collection.immutable.Seq[com.outworkers.phantom.keys.SASIIndex[_ <: com.outworkers.phantom.builder.query.sasi.Mode]] = scala.collection.immutable.Seq.apply[com.outworkers.phantom.keys.SASIIndex[_ <: com.outworkers.phantom.builder.query.sasi.Mode]]()
    };
    ((new anon$macro$1()): _root_.com.outworkers.phantom.macros.TableHelper.Aux[Recipes, Recipe, Recipe :: shapeless.HNil])
}

```

### `com.outworkers.phantom.macros.debug.Options.ShowLog` flag

This flag will cause a lot of the internal compilation log to be revealed, and this is also sometimes useful when
trying to debug.


It will reveal how the match is computed between table fields and case class fields for `CassandraTable`. The first
lines that our printed show `rec.$fieldName -> table.$fieldName` associations, so you can see which case class field
will be extracted fom which table column, according to phantom.

It looks like this:

```
[info] rec.description -> table.description | Option[String]
[info] rec.ingredients -> table.ingredients | List[String]
[info] rec.servings -> table.servings | Option[Int]
[info] rec.lastCheckedAt -> table.lastcheckedat | org.joda.time.DateTime
[info] rec.props -> table.props | scala.collection.immutable.Map[String,String]
[info] rec.uid -> table.uid | java.util.UUID
[info] abstract class Recipes extends Table[Recipes, Recipe] {
[info]                                ^
[info] /Users/../phantom-dsl/src/test/scala/com/outworkers/phantom/tables/Recipes.scala:34: Inferred store input type: com.outworkers.phantom.tables.Recipe :: shapeless.HNil for com.outworkers.phantom.tables.Recipes
[info] abstract class Recipes extends Table[Recipes, Recipe] {
[info]                                ^
[info] /Users/../phantom-dsl/src/test/scala/com/outworkers/phantom/tables/Recipes.scala:34: Altering table name with strategy com.outworkers.phantom.NamingStrategy.identityStrategy
[info] abstract class Recipes extends Table[Recipes, Recipe] {
```

### `com.outworkers.phantom.macros.debug.Options.ShowCache` flag

This is mostly an internal flag, but it allows you to peak in to the implicit resolution mechanism in phantom. The various
macros are not always trivial, and we cache intermediary results to make sure we don't try to workout the same thing twice.


This is again triggered, like all other options, by importing the relevant implicit in the scope where you want to see the 
cache.

```scala
import com.outworkers.phantom.macros.debug.Options.ShowCache
```
If you want to see everything for an entire file, import it at the top of the file, and it will trigger for every table.

Reading the log it's easy to tell if a request from the same implicit evidence was retrieved from cache or had to be computed.

```
[info] /Users/../phantom/phantom-dsl/src/test/scala/com/outworkers/phantom/tables/Recipes.scala:54: ShowCache: _root_.com.outworkers.phantom.builder.primitives.Primitives.UUIDPrimitive cached result _root_.com.outworkers.phantom.builder.primitives.Primitives.UUIDPrimitive
[info]   object id extends UUIDColumn with PartitionKey
[info]                     ^
[info] /Users/../phantom/phantom-dsl/src/test/scala/com/outworkers/phantom/tables/Recipes.scala:55: ShowCache: Long computed result _root_.com.outworkers.phantom.builder.primitives.Primitives.LongPrimitive
[info]   object map extends MapColumn[Long, DateTime]
[info]                      ^
[info] /Users/../phantom/phantom-dsl/src/test/scala/com/outworkers/phantom/tables/Recipes.scala:55: ShowCache: _root_.com.outworkers.phantom.builder.primitives.Primitives.DateTimeIsPrimitive cached result _root_.com.outworkers.phantom.builder.primitives.Primitives.DateTimeIsPrimitive
[info]   object map extends MapColumn[Long, DateTime]
[info]                      ^
[info] /Users/../phantom/phantom-dsl/src/test/scala/com/outworkers/phantom/tables/Recipes.scala:55: ShowCache: Map[Long,org.joda.time.DateTime] computed result _root_.com.outworkers.phantom.builder.primitives.Primitives.map[Long, org.joda.time.DateTime]
[info]   object map extends MapColumn[Long, DateTime]
[info]                      ^
[info] /Users/../phantom/phantom-dsl/src/test/scala/com/outworkers/phantom/tables/Recipes.scala:55: ShowCache: _root_.com.outworkers.phantom.builder.primitives.Primitives.LongPrimitive cached result _root_.com.outworkers.phantom.builder.primitives.Primitives.LongPrimitive
[info]   object map extends MapColumn[Long, DateTime]
[info]                      ^
[info] /Users/../phantom/phantom-dsl/src/test/scala/com/outworkers/phantom/tables/Recipes.scala:55: ShowCache: _root_.com.outworkers.phantom.builder.primitives.Primitives.DateTimeIsPrimitive cached result _root_.com.outworkers.phantom.builder.primitives.Primitives.DateTimeIsPrimitive
[info]   object map extends MapColumn[Long, DateTime]
[info]                      ^
[info] /Users/../phantom/phantom-dsl/src/test/scala/com/outworkers/phantom/tables/Recipes.scala:58: ShowCache: _root_.com.outworkers.phantom.builder.primitives.Primitives.UUIDPrimitive cached result _root_.com.outworkers.phantom.builder.primitives.Primitives.UUIDPrimitive
[info]     select.where(_.id eqs id).one()

```

This is used to optimise compilation times and make sure they stay within normal bounds, and you can use this mechanism
to check if macros in phantom ever become a problem.

### `com.outworkers.phantom.macros.debug.Options.ShowAborts` flag

This is a flag used to force printing aborted implicit resolutions, for when things go wrong. The problem often encountered
is that the compiler is not reliable enough to print its own logs.

Even in very specific scenarios, where we know excatly why certain implicits fail to be found, the compiler still manages to drop the error message,
and you end up receiving a generic `implicit not found`, which is almost never helpful in the case of complex macro derived implicits.

The aborts are force printed using different API methods the compiler seems to like more.

### `com.outworkers.phantom.macros.debug.Options.ShowBoundStatements` flag

By default, prepared statements are not shown in the log. This is because the original query is sent separately to the
database, and after the query is prepared we only send values from the client to Cassandra. This performance
optimisation is the very reason why we use prepared statements.

For debugging reasons however, we can leverage `ShowBoundStatements` to force phantom to produce useful debug output
from prepared statements and all queries we execute against a prepared statement.

**Note**: Unlike all other flags in here, this will influence the runtime logging, not the compile time. Using this
causes different code to be produced behind the scenes, and when binding we generate meaningful logs. Without this flag,
the default code produced by the macros will call `.toString` on a statement which will not produce any information about the values
that were bound. All logs are available under `com.outworkers.phantom`, using SLF4J.

They will look like this:

```
Executing query: INSERT INTO phantom.derivedPrimitivesTable (id, description, rec, complex) VALUES(?, ?, ?, ?) | a015b806-4271-495a-af71-c07199d6f2ce, '3fef15552052713e', '903e44aa-01e0-4ced-b7ac-06ec34b8a21e', ('736e0001-c81c-40da-920f-e04a5e73aa60', 7275279891071387799)

```

It's a more useful way to peak in to what values are passed to a bind. The order of the arguments should always match
the column definition order. Prepared statements in phantom are typechecked, but you can still mix up values of the same type,
if you have more than one text column for instance.

### `com.outworkers.phantom.macros.debug.Options.ShowAll` flag

This flag will trigger all of the above for all of its implicit scope, so wherever you import it it will print everything.
