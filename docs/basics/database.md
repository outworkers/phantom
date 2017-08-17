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
Fhich Cassandra cluster to target.
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
<console>:9: warning: Unused import
       import java.util.UUID
                        ^
<console>:10: warning: Unused import
       import org.joda.time.DateTime
                            ^
<console>:12: warning: Unused import
       import com.outworkers.phantom.dsl._
                                         ^
<console>:17: warning: Unused import
       import com.datastax.driver.core.Session
                                       ^
import com.datastax.driver.core.Session

scala> trait RootConnector {
     | 
     |   implicit def space: KeySpace
     | 
     |   implicit def session: Session
     | }
<console>:9: warning: Unused import
       import java.util.UUID
                        ^
<console>:10: warning: Unused import
       import org.joda.time.DateTime
                            ^
defined trait RootConnector
```

Later on when we start creating databases, we pass in a `ContactPoint` or what we call a `connector` in more plain English, which basically fully encapsulates a Cassandra connection with all the possible details and settings required to run an application.

```scala

import com.outworkers.phantom.dsl._

class RecipesDatabase(
  override val connector: CassandraConnection
) extends Database[RecipesDatabase](connector) {

  object recipes extends Recipes with Connector

}
```

The interesting bit is that the `connector.Connector` is an inner trait inside the `connector` object that will basically statically point all implicit resolutions for a `session` and `keySpace` inside a `database` instance to a specific `session` and `keySpace`.

"It seems a bit complex, why bother to go to such lengths?" The answer to that is simple, in an ideal world:

- You want to invisibly and statically point to the same session object and you want to avoid all possible race conditions.

- E.g you don't want multiple sessions to be instantiated or you don't want your app to connect to Cassandra multiple times just on the basis that a lot of threads are trying to write to Cassandra at the same time.

- You don't want to explicitly refer to `session` every single time, because that's just Java-esque boilerplate. You wouldn't do it in CQL and that's why phantom tries to offer a more fluent DSL instead, the only distinction being in phantom you drive from entities, so the table gets "specified first".

We mask away all that complexity from the end user with the help of a few constructs, `ContactPoint`, `Database` and `DatabaseProvider`.


#### The `DatabaseProvider` injector trait

Sometimes developers can choose to wrap a `database` further, into specific database services that basically move the final destination bit "up one more level", making the services the final level of encapsulation. Why do this? As you will see below, it's useful when separating services for specific entities and for guaranteeing app level consistency of data for de-normalised database entities and indexes.

And this is why we offer another native construct, namely the `DatabaseProvider` trait. This is another really simple but really powerful trait that's generally used cake pattern style.

This is pretty simple in its design, it simply aims to provide a simple way of injecting a reference to a particular `database` inside a consumer. For the sake of argument, let's say we are designing a `UserService` backed by Cassandra and phantom. Here's how it might look like:

```scala
scala> import scala.concurrent.Future
<console>:9: warning: Unused import
       import java.util.UUID
                        ^
<console>:10: warning: Unused import
       import org.joda.time.DateTime
                            ^
<console>:12: warning: Unused import
       import com.outworkers.phantom.dsl._
                                         ^
<console>:14: warning: Unused import
       import com.datastax.driver.core.Session
                                       ^
<console>:16: warning: Unused import
       import com.outworkers.phantom.dsl._
                                         ^
<console>:21: warning: Unused import
       import scala.concurrent.Future
                               ^
import scala.concurrent.Future

scala> import com.outworkers.phantom.dsl._
<console>:9: warning: Unused import
       import java.util.UUID
                        ^
<console>:10: warning: Unused import
       import org.joda.time.DateTime
                            ^
<console>:12: warning: Unused import
       import com.outworkers.phantom.dsl._
                                         ^
<console>:14: warning: Unused import
       import com.datastax.driver.core.Session
                                       ^
<console>:16: warning: Unused import
       import com.outworkers.phantom.dsl._
                                         ^
<console>:18: warning: Unused import
       import scala.concurrent.Future
                               ^
<console>:22: warning: Unused import
       import com.outworkers.phantom.dsl._
                                         ^
import com.outworkers.phantom.dsl._

scala> case class User(id: UUID, email: String, name: String)
<console>:10: warning: Unused import
       import org.joda.time.DateTime
                            ^
<console>:12: warning: Unused import
       import com.outworkers.phantom.dsl._
                                         ^
<console>:14: warning: Unused import
       import com.datastax.driver.core.Session
                                       ^
<console>:16: warning: Unused import
       import com.outworkers.phantom.dsl._
                                         ^
<console>:18: warning: Unused import
       import scala.concurrent.Future
                               ^
defined class User

scala> abstract class Users extends Table[Users, User] {
     |   object id extends UUIDColumn with PartitionKey
     |   object email extends StringColumn
     |   object name extends StringColumn
     | 
     |   def findById(id: UUID): Future[Option[User]] = {
     |     select.where(_.id eqs id).one()
     |   }
     | }
<console>:10: warning: Unused import
       import org.joda.time.DateTime
                            ^
<console>:12: warning: Unused import
       import com.outworkers.phantom.dsl._
                                         ^
<console>:14: warning: Unused import
       import com.datastax.driver.core.Session
                                       ^
<console>:16: warning: Unused import
       import com.outworkers.phantom.dsl._
                                         ^
defined class Users

scala> abstract class UsersByEmail extends Table[UsersByEmail, User] {
     |   object email extends StringColumn with PartitionKey
     |   object id extends UUIDColumn
     |   object name extends StringColumn
     | 
     |   def findByEmail(email: String): Future[Option[User]] = {
     |     select.where(_.email eqs email).one()
     |   }
     | }
<console>:9: warning: Unused import
       import java.util.UUID
                        ^
<console>:10: warning: Unused import
       import org.joda.time.DateTime
                            ^
<console>:12: warning: Unused import
       import com.outworkers.phantom.dsl._
                                         ^
<console>:14: warning: Unused import
       import com.datastax.driver.core.Session
                                       ^
<console>:16: warning: Unused import
       import com.outworkers.phantom.dsl._
                                         ^
defined class UsersByEmail

scala> class AppDatabase(
     |   override val connector: CassandraConnection
     | ) extends Database[AppDatabase](connector) {
     |   object users extends Users with Connector
     |   object usersByEmail extends UsersByEmail with Connector
     | }
<console>:9: warning: Unused import
       import java.util.UUID
                        ^
<console>:10: warning: Unused import
       import org.joda.time.DateTime
                            ^
<console>:12: warning: Unused import
       import com.outworkers.phantom.dsl._
                                         ^
<console>:14: warning: Unused import
       import com.datastax.driver.core.Session
                                       ^
<console>:16: warning: Unused import
       import com.outworkers.phantom.dsl._
                                         ^
<console>:18: warning: Unused import
       import scala.concurrent.Future
                               ^
defined class AppDatabase

scala> // So now we are saying we have a trait
     | // that will eventually provide a reference to a specific database.
     | trait AppDatabaseProvider extends DatabaseProvider[AppDatabase]
<console>:9: warning: Unused import
       import java.util.UUID
                        ^
<console>:10: warning: Unused import
       import org.joda.time.DateTime
                            ^
<console>:12: warning: Unused import
       import com.outworkers.phantom.dsl._
                                         ^
<console>:14: warning: Unused import
       import com.datastax.driver.core.Session
                                       ^
<console>:16: warning: Unused import
       import com.outworkers.phantom.dsl._
                                         ^
<console>:18: warning: Unused import
       import scala.concurrent.Future
                               ^
defined trait AppDatabaseProvider

scala> trait UserService extends AppDatabaseProvider {
     | 
     |   /**
     |    * Stores a user into the database guaranteeing application level consistency of data.
     |    * E.g we have two tables, one indexing users by ID and another indexing users by email.
     |    * As in Cassandra we need to de-normalise data, it's natural we need to store it twice.
     |    * But that also means we have to write to 2 tables every time, and here's how
     |    * @param user A user case class instance.
     |    * @return A future containing the result of the last write operation in the sequence.
     |    */
     |   def store(user: User): Future[ResultSet] = {
     |     for {
     |       byId <- db.users.store(user).future()
     |       byEmail <- db.usersByEmail.store(user).future()
     |     } yield byEmail
     |   }
     | 
     |   def findById(id: UUID): Future[Option[User]] = db.users.findById(id)
     |   def findByEmail(email: String): Future[Option[User]] = db.usersByEmail.findByEmail(email)
     | }
<console>:10: warning: Unused import
       import org.joda.time.DateTime
                            ^
<console>:12: warning: Unused import
       import com.outworkers.phantom.dsl._
                                         ^
<console>:14: warning: Unused import
       import com.datastax.driver.core.Session
                                       ^
<console>:16: warning: Unused import
       import com.outworkers.phantom.dsl._
                                         ^
defined trait UserService
```

If I as your colleague and developer would now want to consume the `UserService`, I would basically create an instance or use a pre-existing one to basically consume methods that only require passing in known domain objects as parameters. Notice how `session`, `keySpace` and everything else Cassandra specific has gone away?

All I can see is a `def storeUser(user: User)` which is all very sensible, so the entire usage of Cassandra is now transparent to end consumers. That's a really cool thing, and granted there are a few hoops to jump through to get here, it's hopefully worth the mileage.

Pretty much the only thing left is the `ResultSet`, and we can get rid of that too should we choose to map it to a domain specific class. It could be useful if we want to hide the fact that we are using Cassandra completely from any database service consumer.


#### Using the DatabaseProvider to specify environments

Ok, so now we have all the elements in place to create the cake pattern, the next step is to basically flesh out the environments we want. In almost 99% of all cases, we only have two provider traits in our entire app, one for `production` or runtime mode, the other for `test`, since we often want a test cluster to fire requests against during the `sbt test` phase.

Let's go ahead and create two complete examples. We are going to make some simple assumptions about how settings for Cassandra look like in production/runtime vs tests, but don't take those seriously, they are just for example purposes more than anything, to show you what you can do with the phantom API.

Let's look at the most basic example of defining a test connector, which will use all default settings plus a call to `noHearbeat` which will disable heartbeats by setting a pooling option to 0 inside the `ClusterBuilder`. We will go through that in more detail in a second, to show how we can specify more complex options using `ContactPoint`.

```scala
scala> import com.outworkers.phantom.dsl._
<console>:9: warning: Unused import
       import java.util.UUID
                        ^
<console>:10: warning: Unused import
       import org.joda.time.DateTime
                            ^
<console>:12: warning: Unused import
       import com.outworkers.phantom.dsl._
                                         ^
<console>:14: warning: Unused import
       import com.datastax.driver.core.Session
                                       ^
<console>:16: warning: Unused import
       import com.outworkers.phantom.dsl._
                                         ^
<console>:18: warning: Unused import
       import scala.concurrent.Future
                               ^
<console>:20: warning: Unused import
       import com.outworkers.phantom.dsl._
                                         ^
<console>:25: warning: Unused import
       import com.outworkers.phantom.dsl._
                                         ^
import com.outworkers.phantom.dsl._

scala> object TestConnector {
     |   val connector = ContactPoint.local
     |     .noHeartbeat()
     |     .keySpace("myapp_example")
     | }
<console>:9: warning: Unused import
       import java.util.UUID
                        ^
<console>:10: warning: Unused import
       import org.joda.time.DateTime
                            ^
<console>:12: warning: Unused import
       import com.outworkers.phantom.dsl._
                                         ^
<console>:14: warning: Unused import
       import com.datastax.driver.core.Session
                                       ^
<console>:16: warning: Unused import
       import com.outworkers.phantom.dsl._
                                         ^
<console>:18: warning: Unused import
       import scala.concurrent.Future
                               ^
<console>:20: warning: Unused import
       import com.outworkers.phantom.dsl._
                                         ^
defined object TestConnector

scala> object TestDatabase extends AppDatabase(TestConnector.connector)
<console>:9: warning: Unused import
       import java.util.UUID
                        ^
<console>:10: warning: Unused import
       import org.joda.time.DateTime
                            ^
<console>:12: warning: Unused import
       import com.outworkers.phantom.dsl._
                                         ^
<console>:14: warning: Unused import
       import com.datastax.driver.core.Session
                                       ^
<console>:16: warning: Unused import
       import com.outworkers.phantom.dsl._
                                         ^
<console>:18: warning: Unused import
       import scala.concurrent.Future
                               ^
<console>:20: warning: Unused import
       import com.outworkers.phantom.dsl._
                                         ^
<console>:24: warning: Unused import
       import com.outworkers.phantom.dsl._
                                         ^
defined object TestDatabase

scala> trait TestDatabaseProvider extends AppDatabaseProvider {
     |   override def database: AppDatabase = TestDatabase
     | }
<console>:9: warning: Unused import
       import java.util.UUID
                        ^
<console>:10: warning: Unused import
       import org.joda.time.DateTime
                            ^
<console>:12: warning: Unused import
       import com.outworkers.phantom.dsl._
                                         ^
<console>:14: warning: Unused import
       import com.datastax.driver.core.Session
                                       ^
<console>:16: warning: Unused import
       import com.outworkers.phantom.dsl._
                                         ^
<console>:18: warning: Unused import
       import scala.concurrent.Future
                               ^
<console>:20: warning: Unused import
       import com.outworkers.phantom.dsl._
                                         ^
<console>:25: warning: Unused import
       import com.outworkers.phantom.dsl._
                                         ^
defined trait TestDatabaseProvider
```

It may feel verbose or slightly too much at first, but the objects wrapping the constructs are basically working a lot in our favour to guarantee the thread safe just in time init static access to various bits that we truly want to be static. Again, we don't want more than one contact point initialised, more than one session and so on, we want it all crystal clear static from the get go.

And this is how you would use that provider trait now. We're going to assume ScalaTest is the testing framework in use, but of course that doesn't matter.

```scala

import com.outworkers.phantom.dsl._

import org.scalatest.{BeforeAndAfterAll, OptionValues, Matchers, FlatSpec}
import org.scalatest.concurrent.ScalaFutures
import com.outworkers.util.samplers._

class UserServiceTest extends FlatSpec with Matchers with ScalaFutures with BeforeAndAfterAll with OptionValues {

  val userService = new UserService with TestDatabaseProvider {}

  override def beforeAll(): Unit = {
    super.beforeAll()
    // all our tables will now be initialised automatically against the target keyspace.
    userService.database.create()
  }

  it should "store a user using the user service and retrieve it by id and email" in {
    val user = gen[User]

    val chain = for {
      store <- userService.store(user)
      byId <- userService.findById(user.id)
      byEmail <- userService.findByEmail(user.email)
    } yield (byId, byEmail)

    whenReady(chain) { case (byId, byEmail) =>
      byId shouldBe defined
      byId.value shouldEqual user

      byEmail shouldBe defined
      byEmail.value shouldEqual user
    }
  }
}
