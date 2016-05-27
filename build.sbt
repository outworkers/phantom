/*
 * Copyright 2013-2015 Websudos, Limited.
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
import net.virtualvoid.sbt.graph.Plugin.graphSettings

lazy val Versions = new {
  val logback = "1.1.7"
  val util = "0.16.0"
  val json4s = "3.3.0"
  val datastax = "3.0.2"
  val scalatest = "2.2.4"
  val shapeless = "2.2.5"
  val thrift = "0.8.0"
  val finagle = "6.35.0"
  val twitterUtil = "6.33.0"
  val scrooge = "4.7.0"
  val scalatra = "2.3.0"
  val play = "2.4.6"
  val scalameter = "0.6"
  val spark = "1.2.0-alpha3"
  val diesel = "0.3.0"
  val slf4j = "1.7.21"
  val reactivestreams = "1.0.0"
  val akka = "2.3.14"
  val typesafeConfig = "1.2.1"
  val jetty = "9.1.2.v20140210"
  val dispatch = "0.11.0"
  val cassandraUnit = "3.0.0.1"
}

val RunningUnderCi = Option(System.getenv("CI")).isDefined || Option(System.getenv("TRAVIS")).isDefined
val defaultConcurrency = 4

val liftVersion: String => String = {
  s => CrossVersion.partialVersion(s) match {
    case Some((major, minor)) if minor >= 11 => "3.0-M6"
    case _ => "3.0-M1"
  }
}

val scalaMacroDependencies: String => Seq[ModuleID] = {
  s => CrossVersion.partialVersion(s) match {
    case Some((major, minor)) if minor >= 11 => Seq.empty
    case _ => Seq(compilerPlugin("org.scalamacros" % "paradise" % "2.1.0" cross CrossVersion.full))
  }
}

val PerformanceTest = config("perf").extend(Test)
lazy val performanceFilter: String => Boolean = _.endsWith("PerformanceTest")

lazy val noPublishSettings = Seq(
  publish := (),
  publishLocal := (),
  publishArtifact := false
)

lazy val defaultCredentials: Seq[Credentials] = {
  if (!RunningUnderCi) {
    Seq(
      Credentials(Path.userHome / ".bintray" / ".credentials"),
      Credentials(Path.userHome / ".ivy2" / ".credentials")
    )
  } else {
    Seq(
      Credentials(
        realm = "Bintray",
        host = "dl.bintray.com",
        userName = System.getenv("bintray_user"),
        passwd = System.getenv("bintray_password")
      ),
      Credentials(
        realm = "Bintray API Realm",
        host = "api.bintray.com",
        userName = System.getenv("bintray_user"),
        passwd = System.getenv("bintray_password")
      )
    )
  }
}

val sharedSettings: Seq[Def.Setting[_]] = Defaults.coreDefaultSettings ++ Seq(
  organization := "com.websudos",
  scalaVersion := "2.11.8",
  credentials ++= defaultCredentials,
  crossScalaVersions := Seq("2.10.6", "2.11.8"),
  commands in ThisBuild ++= Seq(PublishTasks.mavenPublishingCommand, PublishTasks.bintrayPublishingCommand),
  resolvers ++= Seq(
    "Typesafe repository snapshots" at "http://repo.typesafe.com/typesafe/snapshots/",
    "Typesafe repository releases" at "http://repo.typesafe.com/typesafe/releases/",
    "Sonatype repo" at "https://oss.sonatype.org/content/groups/scala-tools/",
    "Sonatype releases" at "https://oss.sonatype.org/content/repositories/releases",
    "Sonatype snapshots" at "https://oss.sonatype.org/content/repositories/snapshots",
    "Sonatype staging" at "http://oss.sonatype.org/content/repositories/staging",
    "Java.net Maven2 Repository" at "http://download.java.net/maven/2/",
    "Twitter Repository" at "http://maven.twttr.com",
    Resolver.bintrayRepo("websudos", "oss-releases")
  ),
  scalacOptions ++= Seq(
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
  libraryDependencies ++= Seq(
    "ch.qos.logback"               % "logback-classic"                    % Versions.logback,
    "org.slf4j"                    % "log4j-over-slf4j"                   % Versions.slf4j
  ) ++ scalaMacroDependencies(scalaVersion.value),
  fork in Test := true,
  javaOptions in ThisBuild ++= Seq(
    "-Xmx2G",
    "-Djava.net.preferIPv4Stack=true",
    "-Dio.netty.resourceLeakDetection"
  ),
  javaOptions in Test ++= Seq(
    "-Xmx2G",
    "-Djava.net.preferIPv4Stack=true",
    "-Dio.netty.resourceLeakDetection"
  ),
  testFrameworks in PerformanceTest := Seq(new TestFramework("org.scalameter.ScalaMeterFramework")),
  testOptions in Test := Seq(Tests.Filter(x => !performanceFilter(x))),
  testOptions in PerformanceTest := Seq(Tests.Filter(x => performanceFilter(x))),
  fork in PerformanceTest := false,
  parallelExecution in ThisBuild := false
) ++ graphSettings ++
  VersionManagement.newSettings ++
  GitProject.gitSettings ++
  PublishTasks.effectivePublishingSettings


lazy val isJdk8: Boolean = sys.props("java.specification.version") == "1.8"

lazy val addOnCondition: (Boolean, ProjectReference) => Seq[ProjectReference] = (bool, ref) =>
  if (bool) ref :: Nil else Nil

lazy val baseProjectList: Seq[ProjectReference] = Seq(
  phantomDsl,
  phantomExample,
  phantomConnectors,
  phantomFinagle,
  phantomReactiveStreams,
  phantomSbtPlugin,
  phantomThrift,
  phantomZookeeper
)

lazy val fullProjectList = baseProjectList ++ addOnCondition(isJdk8, phantomJdk8)

lazy val phantom = (project in file("."))
  .configs(
    PerformanceTest
  ).settings(
    inConfig(PerformanceTest)(Defaults.testTasks): _*
  ).settings(
    sharedSettings ++ noPublishSettings
  ).settings(
    name := "phantom",
    moduleName := "phantom"
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
  logBuffered in Test := false,
  concurrentRestrictions in Test := Seq(
    Tags.limit(Tags.ForkedTestGroup, defaultConcurrency)
  ),
  libraryDependencies ++= Seq(
    "org.scala-lang"               %  "scala-reflect"                     % scalaVersion.value,
    "com.websudos"                 %% "diesel-engine"                     % Versions.diesel,
    "com.chuusai"                  %% "shapeless"                         % Versions.shapeless,
    "joda-time"                    %  "joda-time"                         % "2.8.1",
    "org.joda"                     %  "joda-convert"                      % "1.8.1",
    "com.datastax.cassandra"       %  "cassandra-driver-core"             % Versions.datastax,
    "com.datastax.cassandra"       %  "cassandra-driver-extras"           % Versions.datastax,
    "org.slf4j"                    % "log4j-over-slf4j"                   % Versions.slf4j,
    "org.scalacheck"               %% "scalacheck"                        % "1.11.5"                        % "test",
    "com.websudos"                 %% "util-lift"                         % Versions.util                   % "test",
    "com.websudos"                 %% "util-testing"                      % Versions.util                   % "test",
    "net.liftweb"                  %% "lift-json"                         % liftVersion(scalaVersion.value) % "test",
    "com.storm-enroute"            %% "scalameter"                        % Versions.scalameter             % "test",
    "ch.qos.logback"               % "logback-classic"                    % Versions.logback                % "test"
  )
).dependsOn(
  phantomConnectors
)

lazy val phantomJdk8 = (project in file("phantom-jdk8"))
  .settings(
    name := "phantom-jdk8",
    moduleName := "phantom-jdk8",
    testOptions in Test += Tests.Argument("-oF"),
    logBuffered in Test := false,
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
      "com.websudos"                 %% "util-testing"                      % Versions.util            % "test, provided"
    )
  )

lazy val phantomFinagle = (project in file("phantom-finagle"))
  .configs(PerformanceTest).settings(
    name := "phantom-finagle",
    moduleName := "phantom-finagle",
    libraryDependencies ++= Seq(
      "com.twitter"                  %% "util-core"                         % Versions.twitterUtil,
      "com.websudos"                 %% "util-testing"                      % Versions.util                   % "test, provided",
      "com.storm-enroute"            %% "scalameter"                        % Versions.scalameter             % "test, provided"
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
      "org.slf4j"                    % "slf4j-log4j12"                      % Versions.slf4j % "test, provided",
      "org.apache.thrift"            % "libthrift"                          % Versions.thrift,
      "com.twitter"                  %% "scrooge-core"                      % Versions.scrooge,
      "com.twitter"                  %% "scrooge-serializer"                % Versions.scrooge,
      "com.websudos"                 %% "util-testing"                      % Versions.util               % "test, provided"
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
  publish := {
    CrossVersion.partialVersion(scalaVersion.value).map {
      case (2, scalaMajor) if scalaMajor >= 11 => false
      case _ => true
    }
  },
  publishMavenStyle := false,
  excludeFilter := {
    CrossVersion.partialVersion(scalaVersion.value) match {
      // if scala 2.11+ is used, quasiquotes are merged into scala-reflect
      case Some((2, scalaMajor)) if scalaMajor >= 11 => NothingFilter
      case _ => AllPassFilter
    }
  },
  sbtPlugin := true,
  libraryDependencies ++= Seq(
    "org.cassandraunit" % "cassandra-unit"  % Versions.cassandraUnit excludeAll (
      ExclusionRule("org.slf4j", "slf4j-log4j12"),
      ExclusionRule("org.slf4j", "slf4j-jdk14")
    )
  )
)

lazy val phantomZookeeper = (project in file("phantom-zookeeper"))
  .settings(
    name := "phantom-zookeeper",
    moduleName := "phantom-zookeeper",
    libraryDependencies ++= Seq(
      "org.xerial.snappy"            % "snappy-java"      % "1.1.1.3",
      "com.websudos"                 %% "util-testing"    % Versions.util            % "test, provided",
      "com.websudos"                 %% "util-zookeeper"  % Versions.util            % "test, provided" excludeAll ExclusionRule("org.slf4j", "slf4j-jdk14")
    )
  ).settings(
    sharedSettings: _*
  ).dependsOn(
    phantomConnectors
  )

lazy val phantomReactiveStreams = (project in file("phantom-reactivestreams"))
  .settings(
    name := "phantom-reactivestreams",
    moduleName := "phantom-reactivestreams",
    libraryDependencies ++= Seq(
      "com.typesafe.play"   %% "play-iteratees"       % Versions.play exclude ("com.typesafe", "config"),
      "com.typesafe.play"   %% "play-streams-experimental" % Versions.play exclude("com.typesafe", "config"),
      "com.typesafe"        % "config"                % Versions.typesafeConfig,
      "org.reactivestreams" % "reactive-streams"      % Versions.reactivestreams,
      "com.typesafe.akka"   %% s"akka-actor"          % Versions.akka,
      "com.websudos"        %% "util-testing"         % Versions.util            % "test, provided",
      "org.reactivestreams" % "reactive-streams-tck"  % Versions.reactivestreams % "test, provided",
      "com.storm-enroute"   %% "scalameter"           % Versions.scalameter      % "test, provided"
    )
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
      "com.websudos"                 %% "util-lift"                         % Versions.util            % "test, provided",
      "com.websudos"                 %% "util-testing"                      % Versions.util            % "test, provided"
    )
  ).settings(
    sharedSettings: _*
  ).dependsOn(
    phantomDsl,
    phantomReactiveStreams,
    phantomThrift,
    phantomZookeeper
  )

lazy val phantomContainerTests = (project in file("phantom-container-test"))
  .settings(
    name := "phantom-container-test",
    moduleName := "phantom-container-test",
    fork := true,
    logBuffered in Test := true,
    concurrentRestrictions in Test := Seq(
      Tags.limit(Tags.ForkedTestGroup, defaultConcurrency)
    ),
    libraryDependencies ++= Seq(
      "org.json4s"                %% "json4s-native"                  % Versions.json4s,
      "org.json4s"                %% "json4s-ext"                     % Versions.json4s,
      "net.liftweb"               %% "lift-webkit"                    % liftVersion(scalaVersion.value),
      "net.liftweb"               %% "lift-json"                      % liftVersion(scalaVersion.value),
      "net.databinder.dispatch"   %% "dispatch-core"                  % Versions.dispatch      % "test",
      "javax.servlet"             % "javax.servlet-api"               % "3.0.1"                % "provided",
      "com.websudos"              %% "util-testing"                   % Versions.util          % "provided"
    )
  ).settings(
    sharedSettings: _*
  ).dependsOn(
    phantomDsl,
    phantomThrift,
    phantomZookeeper
  )
