### Execution backends

Internally, Cassandra queries are handled by the Datastax driver which uses the underlying Guava lib to send queries to Cassandra.
In phantom it is however possible to consume these results with different concurrency backends.

Several are supported, some require additional modules to be added as deps:

- Scala Concurrency, available by default. Queries return `scala.concurrent.Future`.
- Reactive Streams, requires a dependency on `phantom-streams`.
- Play Iteratees, again available via `phantom-streams`.
- Twitter Util Concurrency, requires a dependency on `phantom-finagle`.