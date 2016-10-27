/*
 * Copyright 2013-2017 Outworkers, Limited.
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * - Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 *
 * - Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *
 * - Explicit written consent must be obtained from the copyright owner,
 * Outworkers Limited before any redistribution is made.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
import sbt.Keys._
import sbt._
import com.twitter.sbt._

lazy val Versions = new {
  val logback = "1.1.7"
  val util = "0.22.0"
  val datastax = "3.1.0"
  val scalatest = "2.2.4"
  val shapeless = "2.2.5"
  val thrift = "0.8.0"
  val finagle = "6.37.0"
  val twitterUtil = "6.34.0"
  val scalameter = "0.6"
  val diesel = "0.4.1"
  val scalacheck = "1.13.0"
  val slf4j = "1.7.21"
  val reactivestreams = "1.0.0"
  val jetty = "9.1.2.v20140210"
  val cassandraUnit = "3.0.0.1"
  val javaxServlet = "3.0.1"
  val typesafeConfig = "1.2.1"

  val akka: String => String = {
    s => CrossVersion.partialVersion(s) match {
      case Some((major, minor)) if minor >= 11 && Publishing.isJdk8 => "2.4.10"
      case _ => "2.3.15"
    }
  }

  val lift: String => String = {
    s => CrossVersion.partialVersion(s) match {
      case Some((major, minor)) if minor >= 11 => "3.0-RC3"
      case _ => "3.0-M1"
    }
  }

  val scrooge: String => String = {
    s => CrossVersion.partialVersion(s) match {
      case Some((major, minor)) if minor >= 11 => "4.7.0"
      case _ => "4.7.0"
    }
  }

  val play: String => String = {
    s => CrossVersion.partialVersion(s) match {
      case Some((major, minor)) if minor >= 11 => "2.5.8"
      case _ => "2.4.8"
    }
  }

  val playStreams: String => sbt.ModuleID = {
    s => {
      val v = play(s)
      CrossVersion.partialVersion(s) match {
        case Some((major, minor)) if minor >= 11 && Publishing.isJdk8 => {
          "com.typesafe.play" %% "play-streams" % v
        }
        case Some((major, minor)) if minor >= 11  && !Publishing.isJdk8 => {
          "com.typesafe.play" %% "play-streams-experimental" % "2.4.8"
        }
        case _ => "com.typesafe.play" %% "play-streams-experimental" % v
      }
    }
  }
}

val defaultConcurrency = 4

val scalaMacroDependencies: String => Seq[ModuleID] = {
  s => CrossVersion.partialVersion(s) match {
    case Some((major, minor)) if minor >= 11 => Seq.empty
    case _ => Seq(compilerPlugin("org.scalamacros" % "paradise" % "2.1.0" cross CrossVersion.full))
  }
}

val PerformanceTest = config("perf").extend(Test)
lazy val performanceFilter: String => Boolean = _.endsWith("PerformanceTest")


val sharedSettings: Seq[Def.Setting[_]] = Defaults.coreDefaultSettings ++ Seq(
  organization := "com.outworkers",
  scalaVersion := "2.11.8",
  credentials ++= Publishing.defaultCredentials,
  crossScalaVersions := Seq("2.10.6", "2.11.8"),
  resolvers ++= Seq(
    "Twitter Repository" at "http://maven.twttr.com",
    Resolver.typesafeRepo("releases"),
    Resolver.sonatypeRepo("releases"),
    Resolver.jcenterRepo,
    // Temporary deployment location until mainstream release available
    Resolver.bintrayRepo("outworkers", "oss-releases")
  ),
  scalacOptions ++= Seq(
    "-language:experimental.macros",
    "-language:postfixOps",
    "-language:implicitConversions",
    "-language:reflectiveCalls",
    "-language:higherKinds",
    "-language:existentials",
    "-Yinline-warnings",
    "-Xlint",
    "-deprecation",
    "-feature",
    "-unchecked"
  ),
  logLevel in ThisBuild := Level.Info,
  libraryDependencies ++= Seq(
    "ch.qos.logback" % "logback-classic" % Versions.logback % Test,
    "org.slf4j" % "log4j-over-slf4j" % Versions.slf4j
  ),
  fork in Test := true,
  javaOptions in Test ++= Seq(
    "-Xmx2G",
    "-Djava.net.preferIPv4Stack=true",
    "-Dio.netty.resourceLeakDetection"
  ),
  gitTagName <<= (organization, name, version) map { (o, n, v) =>
    "version=%s".format(v)
  },
  testFrameworks in PerformanceTest := Seq(new TestFramework("org.scalameter.ScalaMeterFramework")),
  testOptions in Test := Seq(Tests.Filter(x => !performanceFilter(x))),
  testOptions in PerformanceTest := Seq(Tests.Filter(performanceFilter)),
  fork in PerformanceTest := false,
  parallelExecution in ThisBuild := false
) ++ VersionManagement.newSettings ++
  GitProject.gitSettings ++
  Publishing.effectiveSettings

lazy val baseProjectList: Seq[ProjectReference] = Seq(
  phantomDsl,
  phantomExample,
  phantomConnectors,
  phantomFinagle,
  phantomReactiveStreams,
  phantomThrift
)

lazy val fullProjectList = baseProjectList ++
  Publishing.addOnCondition(Publishing.isJdk8, phantomJdk8) ++
  Publishing.addOnCondition(Publishing.isTravisScala210, phantomSbtPlugin)

lazy val phantom = (project in file("."))
  .configs(
    PerformanceTest
  ).settings(
    inConfig(PerformanceTest)(Defaults.testTasks): _*
  ).settings(
    sharedSettings ++ Publishing.noPublishSettings
  ).settings(
    name := "phantom",
    moduleName := "phantom",
    pgpPassphrase := Publishing.pgpPass
  ).aggregate(
    fullProjectList: _*
  )

lazy val phantomDsl = (project in file("phantom-dsl")).configs(
  PerformanceTest
).settings(
  inConfig(PerformanceTest)(Defaults.testTasks): _*
).settings(
  sharedSettings: _*
).settings(
  name := "phantom-dsl",
  moduleName := "phantom-dsl",
  testOptions in Test += Tests.Argument("-oF"),
  concurrentRestrictions in Test := Seq(
    Tags.limit(Tags.ForkedTestGroup, defaultConcurrency)
  ),
  unmanagedSourceDirectories in Compile ++= Seq(
    (sourceDirectory in Compile).value / ("scala-2." + {
      CrossVersion.partialVersion(scalaBinaryVersion.value) match {
        case Some((major, minor)) => minor
      }
    })),
  libraryDependencies ++= Seq(
    "org.typelevel" %% "macro-compat" % "1.1.1",
    "org.scala-lang" % "scala-compiler" % scalaVersion.value % "provided",
    compilerPlugin("org.scalamacros" % "paradise" % "2.1.0" cross CrossVersion.full),
    "com.outworkers"               %% "diesel-engine"                     % Versions.diesel,
    "com.chuusai"                  %% "shapeless"                         % Versions.shapeless,
    "joda-time"                    %  "joda-time"                         % "2.9.4",
    "org.joda"                     %  "joda-convert"                      % "1.8.1",
    "com.datastax.cassandra"       %  "cassandra-driver-core"             % Versions.datastax,
    "com.datastax.cassandra"       %  "cassandra-driver-extras"           % Versions.datastax,
    "org.scalacheck"               %% "scalacheck"                        % Versions.scalacheck             % Test,
    "com.outworkers"               %% "util-lift"                         % Versions.util                   % Test,
    "com.outworkers"               %% "util-testing"                      % Versions.util                   % Test,
    "com.storm-enroute"            %% "scalameter"                        % Versions.scalameter             % Test
  )
).dependsOn(
  phantomConnectors
)

lazy val phantomJdk8 = (project in file("phantom-jdk8"))
  .settings(
    name := "phantom-jdk8",
    moduleName := "phantom-jdk8",
    testOptions in Test += Tests.Argument("-oF"),
    concurrentRestrictions in Test := Seq(
      Tags.limit(Tags.ForkedTestGroup, defaultConcurrency)
    )
  ).settings(
    sharedSettings: _*
  ).dependsOn(
    phantomDsl % "compile->compile;test->test"
  )

lazy val phantomConnectors = (project in file("phantom-connectors"))
  .configs(PerformanceTest)
  .settings(
    sharedSettings: _*
  ).settings(
    name := "phantom-connectors",
    libraryDependencies ++= Seq(
      "com.datastax.cassandra"       %  "cassandra-driver-core"             % Versions.datastax,
      "com.outworkers"               %% "util-testing"                      % Versions.util % Test
    )
  )

lazy val phantomFinagle = (project in file("phantom-finagle"))
  .configs(PerformanceTest).settings(
    name := "phantom-finagle",
    moduleName := "phantom-finagle",
    libraryDependencies ++= Seq(
      "com.twitter"                  %% "util-core"                         % Versions.twitterUtil,
      "com.outworkers"               %% "util-testing"                      % Versions.util % Test,
      "com.storm-enroute"            %% "scalameter"                        % Versions.scalameter % Test
    )
  ).settings(
    inConfig(PerformanceTest)(Defaults.testTasks) ++ sharedSettings: _*
  ).dependsOn(
    phantomDsl % "compile->compile;test->test"
  )

lazy val phantomThrift = (project in file("phantom-thrift"))
  .settings(
    name := "phantom-thrift",
    moduleName := "phantom-thrift",
    libraryDependencies ++= Seq(
      "org.apache.thrift"            % "libthrift"                          % Versions.thrift,
      "com.twitter"                  %% "scrooge-core"                      % Versions.scrooge(scalaVersion.value),
      "com.twitter"                  %% "scrooge-serializer"                % Versions.scrooge(scalaVersion.value),
      "com.outworkers"               %% "util-testing"                      % Versions.util % Test
    )
  ).settings(
    sharedSettings: _*
  ).dependsOn(
    phantomDsl,
    phantomFinagle
  )

lazy val phantomSbtPlugin = (project in file("phantom-sbt"))
  .settings(
    sharedSettings: _*
  ).settings(
  name := "phantom-sbt",
  moduleName := "phantom-sbt",
  scalaVersion := "2.10.6",
  unmanagedSourceDirectories in Compile ++= Seq(
    (sourceDirectory in Compile).value / ("scala-2." + {
      CrossVersion.partialVersion(scalaBinaryVersion.value) match {
        case Some((major, minor)) => minor
        case None => "10"

      }
  })),
  publish := {
    CrossVersion.partialVersion(scalaVersion.value).map {
      case (2, scalaMajor) if scalaMajor >= 11 => false
      case _ => true
    }
  },
  publishMavenStyle := false,
  sbtPlugin := true,
  libraryDependencies ++= Seq(
    "org.cassandraunit" % "cassandra-unit"  % Versions.cassandraUnit excludeAll (
      ExclusionRule("org.slf4j", "slf4j-log4j12"),
      ExclusionRule("org.slf4j", "slf4j-jdk14")
    )
  )
)

lazy val phantomReactiveStreams = (project in file("phantom-reactivestreams"))
  .settings(
    name := "phantom-reactivestreams",
    moduleName := "phantom-reactivestreams",
    libraryDependencies ++= Seq(
      "com.typesafe.play"   %% "play-iteratees" % Versions.play(scalaVersion.value) exclude ("com.typesafe", "config"),
      Versions.playStreams(scalaVersion.value) exclude ("com.typesafe", "config"),
      "org.reactivestreams" % "reactive-streams"            % Versions.reactivestreams,
      "com.typesafe.akka"   %% s"akka-actor"                % Versions.akka(scalaVersion.value),
      "com.outworkers"      %% "util-testing"               % Versions.util            % Test,
      "org.reactivestreams" % "reactive-streams-tck"        % Versions.reactivestreams % Test,
      "com.storm-enroute"   %% "scalameter"                 % Versions.scalameter      % Test
    ) ++ {
      if (Publishing.isJdk8) {
        Seq("com.typesafe" % "config" % Versions.typesafeConfig)
      } else {
        Seq.empty
      }
    }
  ).settings(
    sharedSettings: _*
  ).dependsOn(
    phantomDsl % "compile->compile;test->test"
  )

lazy val phantomExample = (project in file("phantom-example"))
  .settings(
    name := "phantom-example",
    moduleName := "phantom-example",
    libraryDependencies ++= Seq(
      "com.outworkers"               %% "util-lift"                         % Versions.util % Test,
      "com.outworkers"               %% "util-testing"                      % Versions.util % Test
    )
  ).settings(
    sharedSettings: _*
  ).dependsOn(
    phantomDsl,
    phantomReactiveStreams,
    phantomThrift
  )
