phantom [![Build Status](https://travis-ci.org/websudos/phantom.svg?branch=develop)](https://travis-ci.org/websudos/phantom) [![Coverage Status](https://coveralls.io/repos/websudos/phantom/badge.svg)](https://coveralls.io/r/websudos/phantom) [![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.websudos/phantom_2.10/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.websudos/phantom_2.10) [![Bintray](https://api.bintray.com/packages/websudos/oss-releases/phantom/images/download.svg) ](https://bintray.com/websudos/oss-releases/phantom/_latestVersion) [![Gitter](https://badges.gitter.im/Join%20Chat.svg)](https://gitter.im/websudos/phantom?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)
==================================================================================================

Reactive type-safe Scala DSL for Cassandra

To stay up-to-date with our latest releases and news, follow us on Twitter: [@websudos](https://twitter.com/websudos).

If you use phantom, please consider adding your company to our list of adopters. Phantom is and will always be completely free and open source, 
but the more adopters our projects have, the more people from our company will actively work to make them better.


![phantom](https://s3-eu-west-1.amazonaws.com/websudos/oss/logos/phantom.png "Websudos Phantom")


Using phantom
=============

### Scala 2.10 and 2.11 releases ###

We publish phantom in 2 formats, stable releases and bleeding edge.

- The stable release is always available on Maven Central and will be indicated by the badge at the top of this readme. The Maven Central badge is pointing at the latest version

- Intermediary releases are available through our managed Bintray repository available at `https://dl.bintray.com/websudos/oss-releases/`. The latest version available on our Bintray repository is indicated by the Bintray badge at the top of this readme.


### Latest versions

Check the badges at the top of this README for the latest version. The badges are automatically updated in realtime, where as this README isn't.

- Latest stable version: [![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.websudos/phantom_2.10/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.websudos/phantom_2.10) (Maven Central)
- Bleeding edge: [![Bintray](https://api.bintray.com/packages/websudos/oss-releases/phantom/images/download.svg) ](https://bintray.com/websudos/oss-releases/phantom/_latestVersion) (Websudos OSS releases on Bintray)

<a id="learning-phantom">Tutorials on phantom and Cassandra</a>
======================================================================

For ease of use and far better management of documentation, we have decided to export the `README.md` to a proper
Wiki page, now available [here](https://github.com/websudos/phantom/wiki/). 

The following are the current resources available for learning phantom, outside of tests which are very useful in
highlighting all the possible features in phantom and how to use them.




This is a list of resources to help you learn phantom and Cassandra:

- [ ] [Datastax Introduction to Cassandra](http://www.datastax.com/documentation/getting_started/doc/getting_started/gettingStartedIntro_r.html).
- [ ] [The official Scala API docs for phantom](http://phantom-docs.s3-website-eu-west-1.amazonaws.com/)
- [ ] [The main Wiki](https://github.com/websudos/phantom/wiki)
- [ ] The StackOverflow [phantom-dsl](http://stackoverflow.com/questions/tagged/phantom-dsl) tag, which we always monitor!
- [ ] [A series on Cassandra: Getting rid of the SQL mentality](http://blog.websudos.com/2014/08/16/a-series-on-cassandra-part-1-getting-rid-of-the-sql-mentality/)
- [ ] [A series on Cassandra: Indexes and keys](http://blog.websudos.com/2014/08/23/a-series-on-cassandra-part-2-indexes-and-keys/)
- [ ] [A series on Cassandra: Advanced features](http://blog.websudos.com/2014/10/29/a-series-on-cassandra-part-3-advanced-features/)
- [ ] [A series on phantom: Getting started with phantom](http://blog.websudos.com/2015/04/04/a-series-on-phantom-part-1-getting-started-with-phantom/)
- [ ] [The Play! Phantom Activator template](https://github.com/websudos/phantom-activator-template)
- [ ] [Thiago's Cassandra + Phantom demo repository](https://github.com/thiagoandrade6/cassandra-phantom) 



<a id="issues-and-questions">Issues and questions</a>
=====================================================
<a href="#table-of-contents">back to top</a>

We love Cassandra to bits and use it in every bit of our stack. phantom makes it super trivial for Scala users to embrace Cassandra.

Cassandra is highly scalable and it is by far the most powerful database technology available, open source or otherwise.

Phantom is built on top of the [Datastax Java Driver](https://github.com/datastax/java-driver), which does most of the heavy lifting. 

We are very happy to help implement missing features in phantom, answer questions about phantom, and occasionally help you out with Cassandra questions! Please use GitHub for any issues or bug reports.

Adopters
========

This is a list of companies that have embraced phantom as part of their technology stack and using it in production environments.

- [CreditSuisse](https://www.credit-suisse.com/global/en/)
- [ING](http://www.ing.com/en.htm)
- [Wincor Nixdorf](http://www.wincor-nixdorf.com/internet/site_EN/EN/Home/homepage_node.html)
- [Mobli](https://www.mobli.com/)
- [Pellucid Analytics](http://www.pellucid.com/)
- [Equens](http://www.equens.com/)
- [websudos](https://www.websudos.com/)
- [VictorOps](http://www.victorops.com/)
- [Socrata](http://www.socrata.com) 
- [Sphonic](http://www.sphonic.com/)
- [Anomaly42](http://www.anomaly42.com/)
- [Tecsisa](http://www.tecsisa.com/en/)
- [Tuplejump](http://www.tuplejump.com/)
- [FiloDB](http://www.github.com/tuplejump/FiloDB) - the fast analytics database built on Cassandra and Spark

<a id="contributors">Contributors</a>
=====================================
<a href="#table-of-contents">back to top</a>

Phantom was developed at websudos as an in-house project. All Cassandra integration at Websudos goes through phantom, and nowadays it's safe to say most
Scala/Cassandra users in the world rely on phantom.

* Flavian Alexandru ([@alexflav23](https://github.com/alexflav23)) - maintainer
* Bartosz Jankiewicz ([@bjankie1](https://github.com/bjankie1))
* Benjamin Edwards ([@benjumanji](https://github.com/benjumanji))
* Eugene Zhulenev ([@ezhulenev](https://github.com/ezhulenev))
* Michal Matloka ([@mmatloka](https://github.com/mmatloka))
* Juan José Vázquez ([@juanjovazquez](https://github.com/juanjovazquez))
* Viktor Taranenko ([@viktortnk](https://github.com/viktortnk))
* Stephen Samuel ([@sksamuel](https://github.com/sksamuel))
* Tomasz Perek ([@tperek](https://github.com/tperek))
* Evan Chan ([@evanfchan](https://github.com/evanfchan))
* Jens Halm ([@jenshalm](https://github.com/jenshalm))

<a id="copyright">Copyright</a>
===============================
<a href="#table-of-contents">back to top</a>

Special thanks to Viktor Taranenko from WhiskLabs, who gave us the original idea.

Copyright &copy; 2013 - 2016 websudos.


Contributing to phantom
=======================
<a href="#table-of-contents">back to top</a>

Contributions are most welcome! Use GitHub for issues and pull requests and we will happily help out in any way we can!

YourKit Java Profiler
==================

![yourkit](https://s3-eu-west-1.amazonaws.com/websudos/oss/yklogo.png "YourKit Java Profiler")

We are very grateful to have the open source license support of YourKit, the most advanced Java profiler.

YourKit is the very core of our performance bottleneck testing, and without it phantom would still be a painfully slow tool.

[YourKit Java profiler](https://www.yourkit.com/java/profiler/index.jsp)
