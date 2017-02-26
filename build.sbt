/*
 * Copyright 2013 - 2017 Outworkers Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import sbt.Keys._
import sbt._
import com.twitter.sbt._

lazy val Versions = new {
  val logback = "1.2.1"
  val util = "0.30.1"
  val json4s = "3.5.0"
  val datastax = "3.1.0"
  val scalatest = "3.0.0"
  val shapeless = "2.3.2"
  val thrift = "0.8.0"
  val finagle = "6.37.0"
  val scalameter = "0.8+"
  val scalacheck = "1.13.4"
  val slf4j = "1.7.21"
  val reactivestreams = "1.0.0"
  val cassandraUnit = "3.0.0.1"
  val javaxServlet = "3.0.1"
  val typesafeConfig = "1.3.1"
  val joda = "2.9.7"
  val jodaConvert = "1.8.1"
  val scalamock = "3.4.2"
  val macrocompat = "1.1.1"
  val macroParadise = "2.1.0"

  val twitterUtil: String => String = {
    s => CrossVersion.partialVersion(s) match {
      case Some((_, minor)) if minor >= 12 => "6.39.0"
      case _ => "6.34.0"
    }
  }

  val akka: String => String = {
    s => CrossVersion.partialVersion(s) match {
      case Some((_, minor)) if minor >= 11 && Publishing.isJdk8 => "2.4.14"
      case _ => "2.3.15"
    }
  }

  val lift: String => String = {
    s => CrossVersion.partialVersion(s) match {
      case Some((_, minor)) if minor >= 11 => "3.0"
      case _ => "3.0-M1"
    }
  }

  val scrooge: String => String = {
    s => CrossVersion.partialVersion(s) match {
      case Some((_, minor)) if minor >= 11 => "4.7.0"
      case _ => "4.7.0"
    }
  }

  val play: String => String = {
    s => CrossVersion.partialVersion(s) match {
      case Some((_, minor)) if minor >= 11 => "2.5.8"
      case _ => "2.4.8"
    }
  }

  val playStreams: String => sbt.ModuleID = {
    s => {
      val v = play(s)
      CrossVersion.partialVersion(s) match {
        case Some((_, minor)) if minor >= 11 && Publishing.isJdk8 => {
          "com.typesafe.play" %% "play-streams" % v
        }
        case Some((_, minor)) if minor >= 11  && !Publishing.isJdk8 => {
          "com.typesafe.play" %% "play-streams-experimental" % "2.4.8"
        }
        case _ => "com.typesafe.play" %% "play-streams-experimental" % v
      }
    }
  }
}
val defaultConcurrency = 4

val PerformanceTest = config("perf").extend(Test)

lazy val performanceFilter: String => Boolean = _.endsWith("PerformanceTest")

val sharedSettings: Seq[Def.Setting[_]] = Defaults.coreDefaultSettings ++ Seq(
  organization := "com.outworkers",
  scalaVersion := "2.11.8",
  credentials ++= Publishing.defaultCredentials,
  resolvers ++= Seq(
    "Twitter Repository" at "http://maven.twttr.com",
    Resolver.typesafeRepo("releases"),
    Resolver.sonatypeRepo("releases"),
    Resolver.jcenterRepo
  ),
  scalacOptions in ThisBuild ++= Seq(
    "-language:experimental.macros",
    "-language:postfixOps",
    "-language:implicitConversions",
    "-language:reflectiveCalls",
    "-language:higherKinds",
    "-language:existentials",
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
  phantomStreams,
  phantomThrift
)

lazy val fullProjectList = baseProjectList ++ Publishing.addOnCondition(Publishing.isJdk8, phantomJdk8)

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
  ).enablePlugins(CrossPerProjectPlugin)

lazy val phantomDsl = (project in file("phantom-dsl")).configs(
  PerformanceTest
).settings(
  inConfig(PerformanceTest)(Defaults.testTasks): _*
).settings(
  sharedSettings: _*
).settings(
  name := "phantom-dsl",
  moduleName := "phantom-dsl",
  concurrentRestrictions in Test := Seq(
    Tags.limit(Tags.ForkedTestGroup, defaultConcurrency)
  ),
  crossScalaVersions := Seq("2.10.6", "2.11.8", "2.12.1"),
  libraryDependencies ++= Seq(
    "org.typelevel" %% "macro-compat" % Versions.macrocompat,
    "org.scala-lang" % "scala-compiler" % scalaVersion.value % "provided",
    compilerPlugin("org.scalamacros" % "paradise" % Versions.macroParadise cross CrossVersion.full),
    "com.chuusai"                  %% "shapeless"                         % Versions.shapeless,
    "joda-time"                    %  "joda-time"                         % Versions.joda,
    "org.joda"                     %  "joda-convert"                      % Versions.jodaConvert,
    "com.datastax.cassandra"       %  "cassandra-driver-core"             % Versions.datastax,
    "com.datastax.cassandra"       %  "cassandra-driver-extras"           % Versions.datastax,
    "org.json4s"                   %% "json4s-native"                     % Versions.json4s % Test,
    "org.scalamock"                %% "scalamock-scalatest-support"       % Versions.scalamock % Test,
    "org.scalacheck"               %% "scalacheck"                        % Versions.scalacheck % Test,
    "com.outworkers"               %% "util-testing"                      % Versions.util % Test,
    "com.storm-enroute"            %% "scalameter"                        % Versions.scalameter % Test,
    "ch.qos.logback"               % "logback-classic"                    % Versions.logback % Test
  )
).dependsOn(
  phantomConnectors
).enablePlugins(CrossPerProjectPlugin)

lazy val phantomJdk8 = (project in file("phantom-jdk8"))
  .settings(
    name := "phantom-jdk8",
    moduleName := "phantom-jdk8",
    testOptions in Test += Tests.Argument("-oF"),
    crossScalaVersions := Seq("2.10.6", "2.11.8", "2.12.1"),
    concurrentRestrictions in Test := Seq(
      Tags.limit(Tags.ForkedTestGroup, defaultConcurrency)
    )
  ).settings(
    sharedSettings: _*
  ).dependsOn(
    phantomDsl % "compile->compile;test->test"
  ).enablePlugins(CrossPerProjectPlugin)

lazy val phantomConnectors = (project in file("phantom-connectors"))
  .configs(PerformanceTest)
  .settings(
    sharedSettings: _*
  ).settings(
    name := "phantom-connectors",
    crossScalaVersions := Seq("2.10.6", "2.11.8", "2.12.1"),
    libraryDependencies ++= Seq(
      "com.datastax.cassandra"       %  "cassandra-driver-core"             % Versions.datastax,
      "com.outworkers"               %% "util-testing"                      % Versions.util % Test
    )
  ).enablePlugins(CrossPerProjectPlugin)

lazy val phantomFinagle = (project in file("phantom-finagle"))
  .configs(PerformanceTest).settings(
    name := "phantom-finagle",
    moduleName := "phantom-finagle",
    crossScalaVersions := Seq("2.10.6", "2.11.8"),
    libraryDependencies ++= Seq(
      "com.twitter"                  %% "util-core"                         % Versions.twitterUtil(scalaVersion.value),
      "com.outworkers"               %% "util-testing"                      % Versions.util % Test,
      "com.storm-enroute"            %% "scalameter"                        % Versions.scalameter % Test
    )
  ).settings(
    inConfig(PerformanceTest)(Defaults.testTasks) ++ sharedSettings: _*
  ).dependsOn(
    phantomDsl % "compile->compile;test->test"
  ).enablePlugins(CrossPerProjectPlugin)

lazy val phantomThrift = (project in file("phantom-thrift"))
  .settings(
    name := "phantom-thrift",
    moduleName := "phantom-thrift",
    crossScalaVersions := Seq("2.10.6", "2.11.8"),
    libraryDependencies ++= Seq(
      "org.apache.thrift"            % "libthrift"                          % Versions.thrift,
      "com.twitter"                  %% "scrooge-core"                      % Versions.scrooge(scalaVersion.value),
      "com.twitter"                  %% "scrooge-serializer"                % Versions.scrooge(scalaVersion.value),
      "com.outworkers"               %% "util-testing"                      % Versions.util % Test,
      "com.outworkers"               %% "util-testing-twitter"              % Versions.util % Test
    )
  ).settings(
    sharedSettings: _*
  ).dependsOn(
    phantomDsl % "compile->compile;test->test;",
    phantomFinagle
  ).enablePlugins(CrossPerProjectPlugin)

lazy val phantomSbtPlugin = (project in file("phantom-sbt"))
  .settings(
    sharedSettings: _*
  ).settings(
    name := "phantom-sbt",
    moduleName := "phantom-sbt",
    crossScalaVersions := Seq("2.10.6"),
    publishMavenStyle := false,
    sbtPlugin := true,
    libraryDependencies ++= Seq(
      "org.cassandraunit" % "cassandra-unit"  % Versions.cassandraUnit excludeAll (
        ExclusionRule("org.slf4j", "slf4j-log4j12"),
        ExclusionRule("org.slf4j", "slf4j-jdk14")
      )
    )
  ).enablePlugins(CrossPerProjectPlugin)

lazy val phantomStreams = (project in file("phantom-streams"))
  .settings(
    name := "phantom-streams",
    moduleName := "phantom-streams",
    crossScalaVersions := Seq("2.10.6", "2.11.8"),
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
  ).enablePlugins(CrossPerProjectPlugin)

lazy val phantomExample = (project in file("phantom-example"))
  .settings(
    name := "phantom-example",
    crossScalaVersions := Seq("2.10.6", "2.11.8"),
    moduleName := "phantom-example",
    libraryDependencies ++= Seq(
      "com.outworkers"               %% "util-lift"                         % Versions.util % Test,
      "com.outworkers"               %% "util-testing"                      % Versions.util % Test
    )
  ).settings(
    sharedSettings: _*
  ).dependsOn(
    phantomDsl % "test->test;compile->compile;",
    phantomStreams,
    phantomThrift
  ).enablePlugins(CrossPerProjectPlugin)
