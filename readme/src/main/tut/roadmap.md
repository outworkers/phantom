| CI  | Test coverage(%) | Code quality | Stable version | ScalaDoc | Chat | Open issues | Average issue resolution time | 
| --- | ---------------- | -------------| -------------- | -------- | ---- | ----------- | ----------------------------- |
| [![Build Status](https://travis-ci.org/outworkers/phantom.svg?branch=develop)](https://travis-ci.org/outworkers/phantom?branch=develop) | [![Coverage Status](https://coveralls.io/repos/github/outworkers/phantom/badge.svg?branch=develop)](https://coveralls.io/github/outworkers/phantom?branch=develop) | [![Codacy Rating](https://api.codacy.com/project/badge/grade/25bee222a7d142ff8151e6ceb39151b4)](https://www.codacy.com/app/flavian/phantom_2) | [![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.outworkers/phantom-dsl_2.11/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.outworkers/phantom-dsl_2.11) | [![ScalaDoc](http://javadoc-badge.appspot.com/com.outworkers/phantom-dsl_2.11.svg?label=scaladoc)](http://javadoc-badge.appspot.com/com.outworkers/phantom-dsl_2.11) | [![Gitter](https://badges.gitter.im/Join%20Chat.svg)](https://gitter.im/outworkers/phantom?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge) | [![Percentage of issues still open](http://isitmaintained.com/badge/open/outworkers/phantom.svg)](http://isitmaintained.com/project/outworkers/phantom "%% of issues still open") | [![Average time to resolve an issue](http://isitmaintained.com/badge/resolution/outworkers/phantom.svg)](http://isitmaintained.com/project/outworkers/phantom "Average time to resolve an issue") |

Roadmap
========

#### Phantom 1.x.x

In maintenance mode, users are actively encouraged to upgrade to 2.0.x series.

#### Phantom 2.0.x series

- [x] Support for case sensitive Cassandra tables and keyspace names.

- [x] Support for tuple columns and collection columns

- [x] SASI Index support.

- [ ] Use `QueryPart` as a building block for schema inference during table auto-generation.

- [ ] Deeper integration of CQL features, such as advanced `USING` clauses, `GROUP BY` and `PER PARTITION LIMIT`.(2.0.0)

- [x] Move documentation back to a branch and to a dedicated versioned website based on Git tags.

- [ ] Added implicit.ly integration to notify our audience of new releases.

- [ ] Bring test coverage to 100%(2.0.0)

- [x] Add ability to specify compile time implicit configuration, such as case sensitive column names.

- [x] A new website with highly improved documentation

- [ ] Versioned documentation browser.

- [x] Replacing Travis Cassandra service with CCM to natively include multi-version testing in our setup.

- [x] Add support for macro derived primitives for complex types.

### Phantom Pro

#### v0.1.0

- [x] Full support for UDTs.
- [x] Support for UDT collection types.
- [x] Support for nested UDTs.
- [x] Cassandra 3.8+ support.


#### v0.3.0

- [ ] Auto-tables, ability to generate queries entirely of out `case class` definitions.
- [ ] Advanced table migrations.
- [ ] Materialised views.
- [ ] Development mode automated schema migrations.
- [ ] Scala language based User defined functions.
- [ ] Scala language based User defined aggregates.
- [ ] Support for automated schema generation during `database.autocreate` for UDT types.
