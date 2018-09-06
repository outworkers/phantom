[![Build Status](https://travis-ci.org/outworkers/phantom.svg?branch=develop)](https://travis-ci.org/outworkers/phantom?branch=develop) [![Coverage Status](https://coveralls.io/repos/github/outworkers/phantom/badge.svg?branch=develop)](https://coveralls.io/github/outworkers/phantom?branch=develop)  [![Codacy Rating](https://api.codacy.com/project/badge/grade/25bee222a7d142ff8151e6ceb39151b4)](https://www.codacy.com/app/flavian/phantom_2) [![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.outworkers/phantom-dsl_2.11/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.outworkers/phantom-dsl_2.11) [![Bintray](https://api.bintray.com/packages/outworkers/oss-releases/phantom-dsl/images/download.svg) ](https://bintray.com/outworkers/oss-releases/phantom-dsl/_latestVersion) [![ScalaDoc](http://javadoc-badge.appspot.com/com.outworkers/phantom-dsl_2.11.svg?label=scaladoc)](http://javadoc-badge.appspot.com/com.outworkers/phantom-dsl_2.11) [![Gitter](https://badges.gitter.im/Join%20Chat.svg)](https://gitter.im/outworkers/phantom?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)
======================================


<a id="collections-and-operators">Collections and operators</a>
================================================================
<a href="#table-of-contents">back to top</a>

Based on the above list of columns, phantom supports CQL 3 modify operations for CQL 3 collections: ```list, set, map```.
All operators will be available in an update query, specifically:

```scala
db.table.update
  .where(_.id eqs someId)
  .modify(_.someList $OPERATOR $args)
  .future()
```

<a id="list-operators">List operators</a>
==========================================



| Name                          | Description                                   |
| ----------------------------- | --------------------------------------------- |
| ```prepend```                 | Adds an item to the head of the list          |
| ```prependAll```              | Adds multiple items to the head of the list   |
| ```append```                  | Adds an item to the tail of the list          |
| ```appendAll```               | Adds multiple items to the tail of the list   |
| ```discard```                 | Removes the given item from the list.         |
| ```discardAll```              | Removes all given items from the list.        |
| ```setIdIx```                 | Updates a specific index in the list          |

<a id="set-operators">Set operators</a>
=======================================

Sets have a better performance than lists, as the Cassandra documentation suggests.
Examples in [SetOperationsTest.scala](https://github.com/websudos/phantom/blob/develop/phantom-dsl/src/test/scala/com/websudos/phantom/builder/query/db/crud/SetOperationsTest.scala).

| Name                          | Description                                   |
| ----------------------------- | --------------------------------------------- |
| ```add```                     | Adds an item to the tail of the set           |
| ```addAll```                  | Adds multiple items to the tail of the set    |
| ```remove ```                 | Removes the given item from the set.          |
| ```removeAll```               | Removes all given items from the set.         |


<a id="map-operators">Map operators</a>
=======================================

Both the key and value types of a Map must be Cassandra primitives.
Examples in [MapOperationsTest.scala](https://github.com/websudos/phantom/blob/develop/phantom-dsl/src/test/scala/com/websudos/phantom/builder/query/db/crud/MapOperationsTest.scala):

| Name                          | Description                                   |
| ----------------------------- | --------------------------------------------- |
| ```put```                     | Adds an (key -> value) pair to the map        |
| ```putAll```                  | Adds multiple (key -> value) pairs to the map |
| `setTo`                       | Updates the value of a key in map, with `Database.table.update.where(_.id eqs id).modify(_.map(key) setTo value).future()`|
