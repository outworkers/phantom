### SASI Index support

Available as of phantom 2.11.0, SASI indexes introduce support for a Cassandra 3.4+ feature, namely SS Table attached
secondary indexes. For more details on the internals of SASI within Cassandra, the details are [here](http://www.doanduyhai.com/blog/?p=2058)
or [here](http://batey.info/cassandra-sasi.html).

SASI was an attempt to improve performance on the more traditional secondary indexing, which is notoriously unreliabl
performance wise after a couple thousand records.


### Using SASI support in phantom.

SASI indexes are natively supported in the standard `phantom-dsl` module, so as long as you have the following in your 
`build.sbt` you will not require any special dependencies.

```scala

val phantomVersion = "2.11.0"

libraryDependencies ++= Seq(
  "com.outworkers" %% "phantom-dsl" % phantomVersion
)
```

Simple example:

```tut

import com.outworkers.phantom.dsl._

abstract class MultiSASITable extends Table[MultiSASITable, MultiSASIRecord] {
  object id extends UUIDColumn with PartitionKey

  object name extends StringColumn with SASIIndex[Mode.Contains] {
    override def analyzer: NonTokenizingAnalyzer[Mode.Contains] = {
      Analyzer.NonTokenizingAnalyzer[Mode.Contains]().normalizeLowercase(true)
    }
  }

  object customers extends IntColumn with SASIIndex[Mode.Sparse] {
    override def analyzer: Analyzer[Mode.Sparse] = Analyzer[Mode.Sparse]()
  }

  object phoneNumber extends StringColumn with SASIIndex[Mode.Prefix] {
    override def analyzer: StandardAnalyzer[Mode.Prefix] = {
      Analyzer.StandardAnalyzer[Mode.Prefix]().skipStopWords(true).enableStemming(true)
    }
  }

  object setCol extends SetColumn[Int]
  object listCol extends ListColumn[String]
}
```

