## phantom

| CI  | Test coverage(%) | Code quality | Stable version | ScalaDoc | Chat | Open issues | Average issue resolution time | 
| --- | ---------------- | -------------| -------------- | -------- | ---- | ----------- | ----------------------------- |
| [![Build Status](https://travis-ci.org/outworkers/phantom.svg?branch=develop)](https://travis-ci.org/outworkers/phantom?branch=develop) | [![Coverage Status](https://coveralls.io/repos/github/outworkers/phantom/badge.svg?branch=develop)](https://coveralls.io/github/outworkers/phantom?branch=develop) | [![Codacy Rating](https://api.codacy.com/project/badge/grade/25bee222a7d142ff8151e6ceb39151b4)](https://www.codacy.com/app/flavian/phantom_2) | [![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.outworkers/phantom-dsl_2.11/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.outworkers/phantom-dsl_2.11) | [![ScalaDoc](http://javadoc-badge.appspot.com/com.outworkers/phantom-dsl_2.11.svg?label=scaladoc)](http://javadoc-badge.appspot.com/com.outworkers/phantom-dsl_2.11) | [![Gitter](https://badges.gitter.im/Join%20Chat.svg)](https://gitter.im/outworkers/phantom?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge) | [![Percentage of issues still open](http://isitmaintained.com/badge/open/outworkers/phantom.svg)](http://isitmaintained.com/project/outworkers/phantom "%% of issues still open") | [![Average time to resolve an issue](http://isitmaintained.com/badge/resolution/outworkers/phantom.svg)](http://isitmaintained.com/project/outworkers/phantom "Average time to resolve an issue") |

Reactive type-safe Scala driver for Apache Cassandra/Datastax Enterprise

To stay up-to-date with our latest releases and news, follow us on Twitter: [@outworkers](https://twitter.com/outworkers_uk).

If you use phantom, please consider adding your company to our list of adopters. Phantom is and will always be open source, but the more adopters our projects have, the more people from our company will actively work to make them better.

![phantom](https://s3-eu-west-1.amazonaws.com/websudos/oss/logos/phantom.png "Outworkers Phantom")

Documentation
===================

If this is your very first time with phantom, a good place to start would be the [quickstart guide](./quickstart.md)

- [Basics](./basics/)
    - [Migrating from older versions](./roadmap.md)
    - [Comparing phantom with other drivers](./comparison.md)
    - [Connecting to Cassandra](./basics/connectors.md)
    - [Understanding the Database construct](./basics/database.md)
    
    - *Defining Cassandra tables * 
        - [Schema DSL](./basics/tables.md)
            - [Indexes](./basics/indexes)
                - [SASI Indexes](./basics/indexes/sasi.md)
        - [Primitive datatypes](./basics/primitives.md)
        - [Json columns](./basics/json_columns.md)
        - [Using Batches in Cassandra](./basics/batches.md)
        - [Advanced debugging](./basics/debugging.md)
    
- [Querying with Phantom](./querying)
    - [Query Options](./querying/options.md)
    - [SELECT queries](./querying/select.md)
        - [Aggregation functions](./querying/aggregation_functions.md)
        
    - [Using the different kinds of Futures](./querying/execution.md)    
- [Commercial](./commercial)
    - [Enterprise level support](./commercial/support.md)

- [Roadmap](./roadmap.md)
- [Changelog](./basics/changelog.md)



Commercial support
===================
We, the people behind phantom, run a software development house specialising in Scala and NoSQL. If you are after enterprise grade
training or support for using phantom, [Outworkers](http://outworkers.com) is here to help!

We offer a comprehensive range of elite Scala development services, including but not limited to:

- Software development
- Remote contractors for hire
- Advanced Scala and Cassandra training

We are huge advocates of open source and we will open source every project we can! To read more about our open source efforts, click [here](http://www.outworkers.com/work).
