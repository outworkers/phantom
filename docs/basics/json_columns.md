phantom
[![Build Status](https://travis-ci.org/outworkers/phantom.svg?branch=develop)](https://travis-ci.org/outworkers/phantom?branch=develop) [![Coverage Status](https://coveralls.io/repos/github/outworkers/phantom/badge.svg?branch=develop)](https://coveralls.io/github/outworkers/phantom?branch=develop)  [![Codacy Rating](https://api.codacy.com/project/badge/grade/25bee222a7d142ff8151e6ceb39151b4)](https://www.codacy.com/app/flavian/phantom_2) [![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.outworkers/phantom-dsl_2.11/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.outworkers/phantom-dsl_2.11) [![Bintray](https://api.bintray.com/packages/outworkers/oss-releases/phantom-dsl/images/download.svg) ](https://bintray.com/outworkers/oss-releases/phantom-dsl/_latestVersion) [![ScalaDoc](http://javadoc-badge.appspot.com/com.outworkers/phantom-dsl_2.11.svg?label=scaladoc)](http://javadoc-badge.appspot.com/com.outworkers/phantom-dsl_2.11) [![Gitter](https://badges.gitter.im/Join%20Chat.svg)](https://gitter.im/outworkers/phantom?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)
===============================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================

This is a section about managing custom type support in phantom. It coveralls modelling scenarios where you wish
to make your types act as primitive to Cassandra. There are several options at hand.


### JSON Columns

One simple way to encode case classes or other Scala types as Cassandra native is to use native JSON support in phantom.
This works in a really simple way, phantom will automatically use implicits to serialize/de-serialize your types
to something Cassandra understands, namely string types.

Let's explore a simple example using the [circe](https://github.com/circe/circe) library.
Phantom does not ship with any particular JSON library, you have complete freedom over what JSON library you use.


```scala

import com.outworkers.phantom.dsl._

import io.circe._
import io.circe.generic.semiauto._
import io.circe.parser._
import io.circe.syntax._

case class JsonRecord(
  prop1: String,
  prop2: String
)

object JsonRecord {

  implicit val jsonDecoder: Decoder[JsonRecord] = deriveDecoder[JsonRecord]
  implicit val jsonEncoder: Encoder[JsonRecord] = deriveEncoder[JsonRecord]
  
  implicit val jsonPrimitive: Primitive[JsonRecord] = {
    Primitive.json[JsonRecord](_.asJson.noSpaces)(decode[JsonRecord](_).right.get)
  }

}

case class JsonClass(
  id: UUID,
  name: String,
  json: JsonRecord,
  optionalJson : Option[JsonRecord],
  jsonList: List[JsonRecord],
  jsonSet: Set[JsonRecord]
)

abstract class JsonTable extends Table[JsonTable, JsonClass] {

  object id extends UUIDColumn with PartitionKey

  object name extends StringColumn

  object json extends JsonColumn[JsonRecord]

  object optionalJson extends OptionalJsonColumn[JsonRecord]

  object jsonList extends JsonListColumn[JsonRecord]

  object jsonSet extends JsonSetColumn[JsonRecord]

}
```
