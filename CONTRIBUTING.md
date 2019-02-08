## phantom

Reactive type-safe Scala DSL for Cassandra - Contributing to phantom

| CI  | Test coverage(%) | Code quality | Stable version | ScalaDoc | Chat | Open issues | Average issue resolution time | 
| --- | ---------------- | -------------| -------------- | -------- | ---- | ----------- | ----------------------------- |
| [![Build Status](https://travis-ci.org/outworkers/phantom.svg?branch=develop)](https://travis-ci.org/outworkers/phantom?branch=develop) | [![Coverage Status](https://coveralls.io/repos/github/outworkers/phantom/badge.svg?branch=develop)](https://coveralls.io/github/outworkers/phantom?branch=develop) | [![Codacy Rating](https://api.codacy.com/project/badge/grade/25bee222a7d142ff8151e6ceb39151b4)](https://www.codacy.com/app/flavian/phantom_2) | [![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.outworkers/phantom-dsl_2.11/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.outworkers/phantom-dsl_2.11) | [![ScalaDoc](http://javadoc-badge.appspot.com/com.outworkers/phantom-dsl_2.11.svg?label=scaladoc)](http://javadoc-badge.appspot.com/com.outworkers/phantom-dsl_2.11) | [![Gitter](https://badges.gitter.im/Join%20Chat.svg)](https://gitter.im/outworkers/phantom?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge) | [![Percentage of issues still open](http://isitmaintained.com/badge/open/outworkers/phantom.svg)](http://isitmaintained.com/project/outworkers/phantom "%% of issues still open") | [![Average time to resolve an issue](http://isitmaintained.com/badge/resolution/outworkers/phantom.svg)](http://isitmaintained.com/project/outworkers/phantom "Average time to resolve an issue") |


Contributing to phantom
=======================
<a href="#table-of-contents">back to top</a>

Contributions are most welcome!

<a id="git-flow">Using GitFlow</a>
==================================

To contribute, simply submit a "Pull request" via GitHub.

We use GitFlow as a branching model and SemVer for versioning.

- When you submit a "Pull request" we require all changes to be squashed.
- You will have to create a new release branch every time changes are made.
- We never merge more than one commit at a time. All the n commits on your feature branch must be squashed.
- We won't look at the pull request until Travis CI says the tests pass, make sure tests go well.
- Add your name to the contributors list, we love to share credits if they are well deserved!
- Add your company to the list of adopters, the more adopters our projects have the more people from our company will work hard to maintain and improve them.

<a id="style-guidelines">Scala Style Guidelines</a>
===================================================

In spirit, we follow the [Twitter Scala Style Guidelines](http://twitter.github.io/effectivescala/).
We will reject your pull request if it doesn't meet code standards, but we'll happily give you a hand to get it right.

Some of the things that will make us seriously frown:

- Blocking when you don't have to. It just makes our eyes hurt when we see useless blocking.
- Testing should be thread safe and fully async, use ```ParallelTestExecution``` if you want to show off.
- Writing tests should use the pre-existing tools, they bring in EmbeddedCassandra, Zookeeper and other niceties, allowing us to run multi-datacenter tests.
- Use the common patterns you already see here, we've done a lot of work to make it easy.
- Don't randomly import stuff. We are very big on alphabetized clean imports.
- Tests must pass on both the Oracle and OpenJDK JVM implementations. The only sensitive bit is the Scala reflection mechanism used to detect columns.


