# Debugging

This section is about the various debug options in phantom, logging, enabling special compilation flags and other such options
that allow you to see what's going on behind the scenes. Most of these options do not apply for earlier, pre 2.0.0 versions of phantom.  


## How to enable logging in phantom

Phantom does not configure a specific logging backend for you, it relies on SLF4J to allow you to choose your own. All
queries are logged through one central logger, namely `com.outworkers.phantom`, and we also create one logger instance
per Cassandra table, to allow you to track queries belonging to a particular table more easily. 


It's worth noting what the most verbose loggers will be in this setup, as you will need to pay attention to them:

- `com.outworkers.phantom`, the phantom specific query logger.
- `com.datastax.driver.core`, the logger for the Datastax Java Driver.
- `io.netty`, the logger for the underlying Netty async client, which manages the low level transports used to talk to Cassandra.

These packages produces the most interesting and verbose part of what you will get using phantom, so it's worth paying
particular attention here and configuring them properly to suit your needs.


### Example: Configuring phantom with Log4J logging.

In the below example, we are configuring Apache Log4J logging to work with Cassandra Unit. This is useful when you
write tests against `phantom-sbt` for instance.

```xml
<?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE log4j:configuration SYSTEM "log4j.dtd">
<log4j:configuration xmlns:log4j="http://jakarta.apache.org/log4j/">
    <appender name="outputConsole" class="org.apache.log4j.ConsoleAppender">
        <param name="Target" value="System.out" />
        <layout class="org.apache.log4j.PatternLayout">
            <param name="ConversionPattern" value="%d [%t] %-5p %c{3} - %m%n" />
        </layout>
    </appender>

    <logger name="org.cassandraunit">
        <level value="debug" />
    </logger>
    <logger name="org.apache.cassandra">
        <level value="error" />
    </logger>

    <logger name="com.datastax.driver.core">
        <level value="error" />
    </logger>

    <logger name="jetty">
        <level value="error" />
    </logger>

    <logger name="com.outworkers.phantom">
        <level value="error"/>
    </logger>

    <logger name="me.prettyprint">
        <level value="error" />
    </logger>

    <root>
        <priority value="error" />
        <appender-ref ref="outputConsole" />
    </root>

</log4j:configuration>
```

### Example: Configuring phantom with Logback

This is a very simple example using the more popular Logback framework


```xml
<configuration debug="true">

    <appender name="STDOUT" class="ch.qos.logback.core.ConsoleAppender">
        <!-- encoders are assigned the type
             ch.qos.logback.classic.encoder.PatternLayoutEncoder by default -->
        <encoder>
            <pattern>%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n</pattern>
        </encoder>
    </appender>

    <root level="warning">
        <appender-ref ref="STDOUT" />
    </root>

    <logger name="com.outworkers.phantom" level="ERROR"/>
    <logger name="com.datastax.driver.core" level="ERROR"/>
    <logger name="io.netty" level="ERROR"/>

</configuration>
```


## Compilation level special log flags

Phantom relies quite heavily on Scala macros to work, which means a lot of logic happens hidden away in compilation time.
Because the macro API is not always consistent, some errors are "masked" and incorrectly hidden by the compiler. This can
prove difficult to debug, which is why we've created a collection of macro debug flags. 

If you are entirely new to macros, it's worth understanding they are effectively used to generate code, for instance
automated JSON formats solely based on the structure of a case class, like `play-json` does with its `Json.format[CaseClass]` syntax.

Phantom uses macros to extract table descriptors, a list of names and types for each Cassandra column, and match that against the case class input to `com.outworkers.phantom.CassandraTable`,
to automatically infer the `fromRow` extractor. Without macros, you would need to do this manually, just like in previous versions of phantom.

A lot of the macros are used to "compute" or template implicits on the fly, which means we programmatically determine
the structure of various implicits entirely using macros, generating actual code behind the scenes. 

*Note*: To use any of these flags, you will need to import them in the right place. Unlike traditional compilation flags,
these flags are not global, they are just normal implicits. You need to make sure they are imported in all the scopes where
you want to get logging information, otherwise the macros will not find the implicit triggers and will skip printing
debug information.

All flags are available under the following package:

```scala
com.outworkers.phantom.macros.debug.Options
