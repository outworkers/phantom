phantom
[![Build Status](https://travis-ci.org/outworkers/phantom.svg?branch=develop)](https://travis-ci.org/outworkers/phantom?branch=develop) [![Coverage Status](https://coveralls.io/repos/github/outworkers/phantom/badge.svg?branch=develop)](https://coveralls.io/github/outworkers/phantom?branch=develop)  [![Codacy Rating](https://api.codacy.com/project/badge/grade/25bee222a7d142ff8151e6ceb39151b4)](https://www.codacy.com/app/flavian/phantom_2) [![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.outworkers/phantom-dsl_2.11/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.outworkers/phantom-dsl_2.11) [![Bintray](https://api.bintray.com/packages/outworkers/oss-releases/phantom-dsl/images/download.svg) ](https://bintray.com/outworkers/oss-releases/phantom-dsl/_latestVersion) [![ScalaDoc](http://javadoc-badge.appspot.com/com.outworkers/phantom-dsl_2.11.svg?label=scaladoc)](http://javadoc-badge.appspot.com/com.outworkers/phantom-dsl_2.11) [![Gitter](https://badges.gitter.im/Join%20Chat.svg)](https://gitter.im/outworkers/phantom?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)
===============================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================
<a id="batch-statements">Batch statements</a>
=============================================

Phantom also brrings in support for batch statements. To use them, see [IterateeBigTest.scala](https://github.com/outworkers/phantom/blob/develop/phantom-dsl/src/test/scala/com/outworkers/phantom/builder/query/db/iteratee/IterateeBigReadPerformanceTest.scala). Before you read further, you should remember **batch statements are not used to improve performance**.

Read [the official docs](http://docs.datastax.com/en/cql/3.1/cql/cql_reference/batch_r.html) for more details, but in short **batches guarantee atomicity and they are about 30% slower on average than parallel writes** at least, as they require more round trips. If you think you're optimising performance with batches, you might need to find alternative means.

We have tested with 10,000 statements per batch, and 1000 batches processed simultaneously. Before you run the test, beware that it takes ~40 minutes.

Batches use lazy iterators and daisy chain them to offer thread safe behaviour. They are not memory intensive and you can expect consistent processing speed even with 1 000 000 statements per batch.

Batches are immutable and adding a new record will result in a new Batch, just like most things Scala, so be careful to chain the calls.

phantom also supports `COUNTER` batch updates and `UNLOGGED` batch updates.


<a id="logged-batch-statements">LOGGED batch statements</a>
===========================================================

```tut

import com.outworkers.phantom.dsl._
import com.outworkers.phantom.batch._

Batch.logged
    .add(db.exampleTable.update.where(_.id eqs someId).modify(_.name setTo "blabla"))
    .add(db.exampleTable.update.where(_.id eqs someOtherId).modify(_.name setTo "blabla2"))
    .future()

```

<a id="counter-batch-statements">COUNTER batch statements</a>
============================================================
<a href="#table-of-contents">back to top</a>

```tut

import com.outworkers.phantom.dsl._
import com.outworkers.phantom.batch._

Batch.counter
    .add(db.exampleTable.update.where(_.id eqs someId).modify(_.someCounter increment 500L))
    .add(db.exampleTable.update.where(_.id eqs someOtherId).modify(_.someCounter decrement 300L))
    .future()
```

Counter operations also offer a standard overloaded operator syntax, so instead of `increment` and `decrement`
you can also use `+=` and `-=` to achieve the same thing.

```tut

import com.outworkers.phantom.dsl._
import com.outworkers.phantom.batch._

Batch.counter
    .add(db.exampleTable.update.where(_.id eqs someId).modify(_.someCounter += 500L))
    .add(db.exampleTable.update.where(_.id eqs someOtherId).modify(_.someCounter _= 300L))
    .future()
```

<a id="unlogged-batch-statements">UNLOGGED batch statements</a>
============================================================

```tut

import com.outworkers.phantom.dsl._
import com.outworkers.phantom.batch._

Batch.unlogged
    .add(db.exampleTable.update.where(_.id eqs someId).modify(_.name setTo "blabla"))
    .add(db.exampleTable.update.where(_.id eqs someOtherId).modify(_.name setTo "blabla2"))
    .future()

```
