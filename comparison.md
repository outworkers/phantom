### Phantom vs other Cassandra drivers


This document aims to offer you an insight into the available tooling
for Scala/Cassandra integrations and hopefully help you pick the right
tool for the job.

The available offering:

- [Datastax Java Driver](https://github.com/datastax/java-driver)
- [Phantom](https://github.com/outworkers/phantom)
- [Spark Cassandra Connector](https://github.com/datastax/spark-cassandra-connector)
- [Quill](https://github.com/getquill/quill)
- [Cassie](https://github.com/twitter/cassie)
- [Cascal](https://github.com/shorrockin/cascal)
- [Astyanax](https://github.com/Netflix/astyanax)

Let's first compare the basic qualities of these available drivers, looking
at a wider range of features and verticals, from how deeply integrated they
are with Cassandra and the support they offer, to their level of activity
and how up to date they are.


| Driver | Language | Async | Commercial support  | Initial release | Reactive Streams | Typesafe | Run type | Cassandra version | Latest version | Activity |
| ------ | -------- | ----- | ---------------- | --------| -------- | ----------------- | -------------- | -------- |
| Datastax | Java | yes | no | 2012 | yes | no | no | runtime | latest | 3.1.0 | High |
| Phantom | Scala | yes | yes | 2013 | yes | yes | no | runtime | latest | 3.1.0 | High |


#### Using phantom versus using the Datastax Java Driver


