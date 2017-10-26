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
  val logback = "1.2.3"
  val util = "0.38.0"
  val json4s = "3.5.1"
  val datastax = "3.3.0"
  val scalatest = "3.0.1"
  val shapeless = "2.3.2"
  val thrift = "0.8.0"
  val finagle = "6.42.0"
  val scalameter = "0.8.2"
  val scalacheck = "1.13.5"
  val slf4j = "1.7.25"
  val reactivestreams = "1.0.0"
  val cassandraUnit = "3.1.3.2"
  val javaxServlet = "3.0.1"
  val joda = "2.9.9"
  val jodaConvert = "1.8.1"
  val scalamock = "3.5.0"
  val macrocompat = "1.1.1"
  val macroParadise = "2.1.0"
  val circe = "0.8.0"

  val scala210 = "2.10.6"
  val scala211 = "2.11.11"
  val scala212 = "2.12.4"
  val scalaAll = Seq(scala210, scala211, scala212)

  val scala = new {
    val all = Seq(scala210, scala211, scala212)
  }

  val typesafeConfig: String = if (Publishing.isJdk8) {
    "1.3.1"
  } else {
    "1.2.0"
  }

  val twitterUtil: String => String = {
    s => CrossVersion.partialVersion(s) match {
      case Some((_, minor)) if minor >= 12 => "6.41.0"
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
      case Some((_, minor)) if minor >= 11 && Publishing.isJdk8 => "4.18.0"
      case Some((_, minor)) if minor >= 11 && !Publishing.isJdk8 => "4.7.0"
      case _ => "4.7.0"
    }
  }
  val play: String => String = {
    s => CrossVersion.partialVersion(s) match {
      case Some((_, minor)) if minor == 12 => "2.6.1"
      case Some((_, minor)) if minor == 11 => "2.5.8"
      case _ => "2.4.8"
    }
  }
}

val defaultConcurrency = 4

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
)

val sharedSettings: Seq[Def.Setting[_]] = Defaults.coreDefaultSettings ++ Seq(
  organization := "com.outworkers",
  scalaVersion := Versions.scala212,
  credentials ++= Publishing.defaultCredentials,
  resolvers ++= Seq(
    "Twitter Repository" at "http://maven.twttr.com",
    Resolver.typesafeRepo("releases"),
    Resolver.sonatypeRepo("releases"),
    Resolver.jcenterRepo
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
  envVars := Map("SCALACTIC_FILL_FILE_PATHNAMES" -> "yes"),
  gitTagName in ThisBuild := s"version=${scalaVersion.value}",
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
  phantomThrift,
  phantomSbtPlugin,
  readme
)

lazy val fullProjectList = baseProjectList ++ Publishing.addOnCondition(Publishing.isJdk8, phantomJdk8)

lazy val phantom = (project in file("."))
  .settings(
    sharedSettings ++ Publishing.noPublishSettings
  ).settings(
    name := "phantom",
    moduleName := "phantom",
    pgpPassphrase := Publishing.pgpPass,
    commands += Command.command("testsWithCoverage") { state =>
      "coverage" ::
      "test" ::
      "coverageReport" ::
      "coverageAggregate" ::
      "coveralls" ::
      state
    }
  ).aggregate(
    fullProjectList: _*
  )

lazy val readme = (project in file("readme"))
  .settings(sharedSettings)
  .settings(
    crossScalaVersions := Seq(Versions.scala211, Versions.scala212),
    tutSourceDirectory := sourceDirectory.value / "main" / "tut",
    tutTargetDirectory := phantom.base / "docs",
    libraryDependencies ++= Seq(
      "org.typelevel" %% "macro-compat" % Versions.macrocompat % "tut",
      "org.scala-lang" % "scala-compiler" % scalaVersion.value % "tut",
      compilerPlugin("org.scalamacros" % "paradise" % Versions.macroParadise cross CrossVersion.full),
      "com.outworkers" %% "util-samplers" % Versions.util % "tut",
      "io.circe" %% "circe-parser" % Versions.circe % "tut",
      "io.circe" %% "circe-generic" % Versions.circe % "tut",
      "org.scalatest" %% "scalatest" % Versions.scalatest % "tut"
    )
  ).dependsOn(
    phantomDsl,
    phantomJdk8,
    phantomExample,
    phantomConnectors,
    phantomFinagle,
    phantomStreams,
    phantomThrift
  ).enablePlugins(TutPlugin, CrossPerProjectPlugin)

lazy val phantomDsl = (project in file("phantom-dsl"))
  .settings(sharedSettings: _*)
  .settings(
    name := "phantom-dsl",
    moduleName := "phantom-dsl",
    crossScalaVersions := Versions.scalaAll,
    concurrentRestrictions in Test := Seq(
      Tags.limit(Tags.ForkedTestGroup, defaultConcurrency)
    ),
    libraryDependencies ++= Seq(
      "org.typelevel" %% "macro-compat" % Versions.macrocompat,
      "org.scala-lang" % "scala-compiler" % scalaVersion.value % "provided",
      compilerPlugin("org.scalamacros" % "paradise" % Versions.macroParadise cross CrossVersion.full),
      "com.chuusai"                  %% "shapeless"                         % Versions.shapeless,
      "joda-time"                    %  "joda-time"                         % Versions.joda,
      "org.joda"                     %  "joda-convert"                      % Versions.jodaConvert,
      "com.datastax.cassandra"       %  "cassandra-driver-core"             % Versions.datastax,
      "org.json4s"                   %% "json4s-native"                     % Versions.json4s % Test,
      "io.circe"                     %% "circe-parser"                      % Versions.circe % Test,
      "io.circe"                     %% "circe-generic"                     % Versions.circe % Test,
      "org.scalamock"                %% "scalamock-scalatest-support"       % Versions.scalamock % Test,
      "org.scalacheck"               %% "scalacheck"                        % Versions.scalacheck % Test,
      "com.outworkers"               %% "util-samplers"                     % Versions.util % Test,
      "com.storm-enroute"            %% "scalameter"                        % Versions.scalameter % Test,
      "ch.qos.logback"               % "logback-classic"                    % Versions.logback % Test
    )
  ).dependsOn(
    phantomConnectors
  ).enablePlugins(
    CrossPerProjectPlugin
  )

lazy val phantomJdk8 = (project in file("phantom-jdk8"))
  .settings(
    name := "phantom-jdk8",
    moduleName := "phantom-jdk8",
    crossScalaVersions := Versions.scalaAll,
    testOptions in Test += Tests.Argument("-oF"),
    concurrentRestrictions in Test := Seq(
      Tags.limit(Tags.ForkedTestGroup, defaultConcurrency)
    ),
    libraryDependencies ++= Seq(
      compilerPlugin("org.scalamacros" % "paradise" % Versions.macroParadise cross CrossVersion.full)
    )
  ).settings(
    sharedSettings: _*
  ).dependsOn(
    phantomDsl % "compile->compile;test->test"
  ).enablePlugins(
    CrossPerProjectPlugin
  )

lazy val phantomConnectors = (project in file("phantom-connectors"))
  .settings(
    sharedSettings: _*
  ).settings(
    name := "phantom-connectors",
    moduleName := "phantom-connectors",
    crossScalaVersions := Versions.scalaAll,
    libraryDependencies ++= Seq(
      "com.datastax.cassandra"       %  "cassandra-driver-core"             % Versions.datastax,
      "com.outworkers"               %% "util-testing"                      % Versions.util % Test
    )
  ).enablePlugins(
   CrossPerProjectPlugin
  )

lazy val phantomFinagle = (project in file("phantom-finagle"))
  .settings(sharedSettings: _*)
  .settings(
    name := "phantom-finagle",
    moduleName := "phantom-finagle",
    crossScalaVersions := Versions.scalaAll,
    testFrameworks in Test ++= Seq(new TestFramework("org.scalameter.ScalaMeterFramework")),
    libraryDependencies ++= Seq(
      compilerPlugin("org.scalamacros" % "paradise" % Versions.macroParadise cross CrossVersion.full),
      "com.twitter"                  %% "util-core"                         % Versions.twitterUtil(scalaVersion.value),
      "com.outworkers"               %% "util-testing"                      % Versions.util % Test,
      "com.outworkers"               %% "util-testing-twitter"              % Versions.util % Test,
      "com.storm-enroute"            %% "scalameter"                        % Versions.scalameter % Test
    )
  ).dependsOn(
    phantomDsl % "compile->compile;test->test"
  ).enablePlugins(
   CrossPerProjectPlugin
  )

lazy val phantomThrift = (project in file("phantom-thrift"))
  .settings(
    crossScalaVersions := Seq(Versions.scala211, Versions.scala212),
    name := "phantom-thrift",
    moduleName := "phantom-thrift",
    addCompilerPlugin("org.scalamacros" % "paradise" % Versions.macroParadise cross CrossVersion.full),
    libraryDependencies ++= Seq(
      "org.typelevel" %% "macro-compat" % Versions.macrocompat,
      "org.scala-lang" % "scala-compiler" % scalaVersion.value % "provided",
      "org.apache.thrift"            % "libthrift"                          % Versions.thrift,
      "com.twitter"                  %% "scrooge-core"                      % Versions.scrooge(scalaVersion.value),
      "com.twitter"                  %% "scrooge-serializer"                % Versions.scrooge(scalaVersion.value),
      "com.outworkers"               %% "util-testing"                      % Versions.util % Test,
      "com.outworkers"               %% "util-testing-twitter"              % Versions.util % Test
    ),
    coverageExcludedPackages := "com.outworkers.phantom.thrift.models.*"
  ).settings(
    sharedSettings: _*
  ).dependsOn(
    phantomDsl % "compile->compile;test->test;",
    phantomFinagle
  ).enablePlugins(
    CrossPerProjectPlugin
  )

lazy val phantomSbtPlugin = (project in file("phantom-sbt"))
  .settings(
    sharedSettings: _*
  ).settings(
    name := "phantom-sbt",
    moduleName := "phantom-sbt",
    crossScalaVersions := Seq(Versions.scala210),
    publishMavenStyle := false,
    sbtPlugin := true,
    publishArtifact := !Publishing.publishingToMaven && { scalaVersion.value.startsWith("2.10") },
    libraryDependencies ++= Seq(
      "com.datastax.cassandra" % "cassandra-driver-core" % Versions.datastax,
      "org.cassandraunit" % "cassandra-unit"  % Versions.cassandraUnit excludeAll (
        ExclusionRule("org.slf4j", "slf4j-log4j12"),
        ExclusionRule("org.slf4j", "slf4j-jdk14")
      )
    )
  ).enablePlugins(
    CrossPerProjectPlugin
  )

lazy val phantomStreams = (project in file("phantom-streams"))
  .settings(
    name := "phantom-streams",
    moduleName := "phantom-streams",
    crossScalaVersions := Versions.scalaAll,
    testFrameworks in Test ++= Seq(new TestFramework("org.scalameter.ScalaMeterFramework")),
    libraryDependencies ++= Seq(
      compilerPlugin("org.scalamacros" % "paradise" % Versions.macroParadise cross CrossVersion.full),
      "com.typesafe" % "config" % Versions.typesafeConfig force(),
      "com.typesafe.play"   %% "play-iteratees" % Versions.play(scalaVersion.value) exclude ("com.typesafe", "config"),
      "org.reactivestreams" % "reactive-streams"            % Versions.reactivestreams,
      "com.typesafe.akka"   %% s"akka-actor"                % Versions.akka(scalaVersion.value) exclude ("com.typesafe", "config"),
      "com.outworkers"      %% "util-testing"               % Versions.util            % Test,
      "org.reactivestreams" % "reactive-streams-tck"        % Versions.reactivestreams % Test,
      "com.storm-enroute"   %% "scalameter"                 % Versions.scalameter      % Test
    )
  ).settings(
    sharedSettings: _*
  ).dependsOn(
    phantomDsl % "compile->compile;test->test"
  ).enablePlugins(
    CrossPerProjectPlugin
  )

lazy val phantomExample = (project in file("phantom-example"))
  .settings(
    name := "phantom-example",
    moduleName := "phantom-example",
    crossScalaVersions := Seq(Versions.scala211, Versions.scala212),
    libraryDependencies ++= Seq(
      compilerPlugin("org.scalamacros" % "paradise" % Versions.macroParadise cross CrossVersion.full),
      "org.json4s"                   %% "json4s-native"                     % Versions.json4s % Test,
      "com.outworkers"               %% "util-samplers"                      % Versions.util % Test
    ),
    coverageExcludedPackages := "com.outworkers.phantom.example.basics.thrift.*"
  ).settings(
    sharedSettings: _*
  ).settings(
    Publishing.noPublishSettings
  ).dependsOn(
    phantomDsl % "test->test;compile->compile;",
    phantomThrift
  ).enablePlugins(
    CrossPerProjectPlugin
  )