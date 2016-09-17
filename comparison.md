### Phantom vs other Cassandra drivers


This document aims to offer you an insight into the available tooling
for Scala/Cassandra integrations and hopefully help you pick the right
tool for the job.

The available offering:

- [Datastax Java Driver](https://github.com/datastax/java-driver)
- [Phantom](https://github.com/outworkers/phantom)
- [Spark Cassandra Connector](https://github.com/datastax/spark-cassandra-connector)
- [Quill](https://github.com/getquill/quill)
- 
Let's first compare the basic qualities of these available drivers, looking
at a wider range of features and verticals, from how deeply integrated they
are with Cassandra and the support they offer, to their level of activity
and how up to date they are.


| Driver | Language | Commercial | Type-safe | Spark Support | Streams | DSL | Cassandra | Latest | Activity | Created |
| ------ | -------- | ----- | ------------------ | --------- | ------------- | ---------------- | --- | -------- | ---- | ----- | ----- |
| Datastax Java Driver | Java | yes | no | no | no | EDSL | 3.8.0 | 3.1.0 | High | 2012 |
| Phantom | Scala | yes | yes | no | yes | EDSL | 3.8.0 | 3.1.0 | High | 2013 |
| Spark Connector | Scala | yes | yes | yes | no | EDSL | 3.0 | High | 2014 |
| Quill | Scala | no | yes | no | yes | QDSL | 3.8.0 | 2015 |


### An overview of the various drivers and using them from Scala

#### Datastax Java Driver

Created by Datastax, the commercial company behind Cassandra, this is the underlying engine of all other drivers. Phantom, Quill and the Spark connector all use it underneath the hood to connect and execute queries. Phantom and Quill add a Scala friendly face to the driver, while the Spark connector does what it says on the tin, namely to focus on Cassandra - Spark integration.

So why not just use the Datastax driver for Scala? We would like to start by saying it's a really really good tool and some seriously good engineering lies behind it, but by sole virtue of being aimed at a Java audience, it does miss out a lot on some of the really powerful things you can do with the Scala compiler.

#### Cons of using the Java driver from Scala

- Writing queries is not type-safe, and the `QueryBuilder` is mutable.
- There is no DSL to define the tables, so dealing with them is still very much manual and string based. 
- Auto-generation of schema is not available, the CQL for tables or anything else must be manually created.
- The driver does not and cannot prevent you from doing bad things.
- You must constantly refer to the `session`.
- You constantly get back a `ResultSet`, which in most instances is not what you want.
- There is not native support for `scala.concurrent.Future` or any of the default Scala primitives.

Overall, if you wanted to, you could naturally use the Java driver inside a Scala application, but you probably wouldn't to. Again, this has very little to do with the quality of the driver itself as much as it has to do with the fact that the API is Java and therefore aimed at a Java consumer audience.

Libraries such as phantom and quill are designed to abstract away many of the internals of the Java driver and make it more appealing to a Scala audience, and phantom goes very very far to mask away all the underlying complexities and provide very advanced support for using Cassandra as a database layer.


#### Phantom

Phantom is designed to be a one stop destination. It is built exclusively to cater to the app level integration of Scala and Cassandra, meaning it is a lot more than a simple driver. It is built on top of the Datastax Java driver, and uses all the default connection strategies and so on under the hood, without exposing any of the Java-esque APIs, but at the same time offering a multitude of features that are not available in the Java driver or in any other driver.

- A type safe Schema DSL that means you never have to deal with raw CQL again.
- A very advanced compile time mechanism that offers a fully type safe variant of CQL.
- A natural DSL that doesn't require any new terminology and aims to introduce a minimal learning curve. Phantom is not a leaking abstraction and it is exclusively built to target Cassnadra integration, therefore it has support for all the latest features of CQL and doesn't require constantly mapping terminology. Unlike LINQ style DSLs for instance, the naming will largely have 100% correspondence to CQL terminology you are already used to.
- Automated schema generation, automated table migrations, automated database generation and more, meaning you will never ever have to manually initialise CQL tables from scripts ever again.
- Native support of Scala concurrency primitives, from `scala.concurrent.Future` to more advanced access patterns such as reactive streams or even iteratees, available via separate dependencies.



