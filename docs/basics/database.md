phantom
[![Build Status](https://travis-ci.org/outworkers/phantom.svg?branch=develop)](https://travis-ci.org/outworkers/phantom?branch=develop) [![Coverage Status](https://coveralls.io/repos/github/outworkers/phantom/badge.svg?branch=develop)](https://coveralls.io/github/outworkers/phantom?branch=develop)  [![Codacy Rating](https://api.codacy.com/project/badge/grade/25bee222a7d142ff8151e6ceb39151b4)](https://www.codacy.com/app/flavian/phantom_2) [![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.outworkers/phantom-dsl_2.11/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.outworkers/phantom-dsl_2.11) [![Bintray](https://api.bintray.com/packages/outworkers/oss-releases/phantom-dsl/images/download.svg) ](https://bintray.com/outworkers/oss-releases/phantom-dsl/_latestVersion) [![ScalaDoc](http://javadoc-badge.appspot.com/com.outworkers/phantom-dsl_2.11.svg?label=scaladoc)](http://javadoc-badge.appspot.com/com.outworkers/phantom-dsl_2.11) [![Gitter](https://badges.gitter.im/Join%20Chat.svg)](https://gitter.im/outworkers/phantom?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)
===============================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================

Phantom offers an interesting first class citizen construct called the `Database` class. It seems quite simple, but it is designed to serve several purposes simultaneously:

- Provide encapsulation and prevent `session` and `keySpace` or other Cassandra/Phantom specific constructs from leaking into other layers of the application.
- Provide a configurable mechanism to allow automated schema generation.
- Provide a type-safe way to fully describe the available domain.
- Provide a way for test code to easily override settings via the cake pattern.

Let's explore some of the design goals in more detail to understand how things work under the hood.

#### Database is designed to be the final level of encapsulation

Being the final level of segregation between the database layer of your application and every other layer, essentially guaranteeing encapsulation. Beyond this point, no other consumer of your database service should ever know that you are using `Cassandra` as a database.

At the very bottom level, phantom queries require several implicits in scope to execute:

- The `implicit session: com.datastax.driver.core.Session`, that tells us which Cassandra cluster to target.
- The `implicit keySpace: KeySpace`, describing which keyspace to target. It's just a `String`, but it's more strongly typed as we don't want `implicit` strings in our code, ever.
- The `implicit ex: ExecutionContextExecutor`, which is a Java compatible flavour of `scala.concurrent.ExecutionContext` and basically allows users to supply any context of their choosing for executing database queries.

However, from an app or service consumer perspective, when pulling in dependencies or calling a database service, as a developer I do not want to be concerned with providing a `session` or a `keySpace`. Under some circumstances I may want to provide a custom `ExecutionContext` but that's a different story.

That's why phantom comes with very concise levels of segregation between the various consumer levels. When we create a table, we mix in `RootConnector`.

```scala

import java.util.UUID
import org.joda.time.DateTime
import com.outworkers.phantom.dsl._

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
```
The whole purpose of `RootConnector` is quite simple, it's saying an implementor will basically specify the `session` and `keySpace` of choice. It looks like this, and it's available in phantom by default via the default import, `import com.outworkers.phantom.dsl._`.

```scala
scala> import com.datastax.driver.core.Session
