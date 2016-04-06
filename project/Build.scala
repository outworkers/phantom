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
 * Websudos Limited before any redistribution is made.
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

import com.twitter.sbt._
import com.twitter.scrooge.ScroogeSBT
import sbt.Keys._
import sbt._

object Build extends Build {

  object Versions {
    val logback = "1.1.3"
    val util = "0.15.0"
    val json4s = "3.3.0"
    val datastax = "3.0.0"
    val scalatest = "2.2.4"
    val shapeless = "2.2.5"
    val finagle = "6.34.0"
    val twitterUtil = "6.33.0"
    val scrooge = "3.17.0"
    val scalatra = "2.3.0"
    val play = "2.4.3"
    val scalameter = "0.6"
    val spark = "1.2.0-alpha3"
    val thrift = "0.5.0"
    val diesel = "0.2.4"
    val slf4j = "1.7.12"
    val reactivestreams = "1.0.0"
    val akka = "2.3.14"
    val typesafeConfig = "1.2.1"
    val jetty = "9.1.2.v20140210"
    val dispatch = "0.11.0"
  }

  val RunningUnderCi = Option(System.getenv("CI")).isDefined || Option(System.getenv("TRAVIS")).isDefined
  final val defaultConcurrency = 4

  println(s"Running under CI status: ${RunningUnderCi}")

  def liftVersion(scalaVersion: String): String = {
    scalaVersion match {
      case "2.10.5" => "3.0-M1"
      case _ => "3.0-M6"
    }
  }
  val PerformanceTest = config("perf").extend(Test)
  def performanceFilter(name: String): Boolean = name endsWith "PerformanceTest"


  def defaultCredentials: Seq[Credentials] = {
    if (!RunningUnderCi) {
      Seq(
        Credentials(Path.userHome / ".bintray" / ".credentials"),
        Credentials(Path.userHome / ".iv2" / ".credentials")
      )
    } else {
      println(s"Bintray publisher username ${System.getenv("bintray_user")}")
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
    version := "1.25.2",
    scalaVersion := "2.11.7",
    credentials ++= defaultCredentials,
    crossScalaVersions := Seq("2.10.5", "2.11.7"),
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
      "ch.qos.logback"               % "logback-classic"                    % "1.1.3",
      "org.slf4j"                    % "log4j-over-slf4j"                   % "1.7.12"
    ),
    addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.0-M5" cross CrossVersion.full),
    fork in Test := false,
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
  ) ++ net.virtualvoid.sbt.graph.Plugin.graphSettings ++
      VersionManagement.newSettings ++
      GitProject.gitSettings ++
      PublishTasks.bintrayPublishSettings


  private[this] def isJdk8: Boolean = sys.props("java.specification.version") == "1.8"

  private[this] def addOnCondition(
    condition: Boolean,
    projectReference: ProjectReference
    ): Seq[ProjectReference] = {
    if (condition) projectReference :: Nil else Nil
  }

  lazy val baseProjectList: Seq[ProjectReference] = Seq(
    phantomDsl,
    phantomExample,
    phantomConnectors,
    phantomReactiveStreams,
    phantomThrift,
    phantomUdt,
    phantomZookeeper
  )

  lazy val fullProjectList = baseProjectList ++ addOnCondition(isJdk8, phantomJdk8)

  lazy val phantom = Project(
    id = "phantom",
    base = file("."),
    settings = sharedSettings
  ).configs(
    PerformanceTest
  ).settings(
    inConfig(PerformanceTest)(Defaults.testTasks): _*
  ).settings(
    name := "phantom"
  ).aggregate(
    fullProjectList: _*
  )

  lazy val phantomDsl = Project(
    id = "phantom-dsl",
    base = file("phantom-dsl"),
    settings = Defaults.coreDefaultSettings ++ sharedSettings
  ).configs(
      PerformanceTest
    ).settings(
      inConfig(PerformanceTest)(Defaults.testTasks): _*
    ).settings(
      name := "phantom-dsl",
      testOptions in Test += Tests.Argument("-oF"),
      logBuffered in Test := false,
      concurrentRestrictions in Test := Seq(
        Tags.limit(Tags.ForkedTestGroup, defaultConcurrency)
      ),
      libraryDependencies ++= Seq(
        "org.scala-lang"               %  "scala-reflect"                     % scalaVersion.value,
        "com.websudos"                 %% "diesel-engine"                     % Versions.diesel,
        "com.chuusai"                  %% "shapeless"                         % Versions.shapeless,
        "com.twitter"                  %% "util-core"                         % Versions.twitterUtil,
        "com.typesafe.play"            %% "play-iteratees"                    % "2.4.0-M1" exclude ("com.typesafe", "config"),
        "joda-time"                    %  "joda-time"                         % "2.3",
        "org.joda"                     %  "joda-convert"                      % "1.6",
        "com.datastax.cassandra"       %  "cassandra-driver-core"             % Versions.datastax,
        "com.datastax.cassandra"       %  "cassandra-driver-extras"           % Versions.datastax,
        "org.slf4j"                    % "log4j-over-slf4j"                   % Versions.slf4j,
        "org.scalacheck"               %% "scalacheck"                        % "1.11.5"                        % "test, provided",
        "com.websudos"                 %% "util-lift"                         % Versions.util                   % "test, provided",
        "com.websudos"                 %% "util-testing"                      % Versions.util                   % "test, provided",
        "net.liftweb"                  %% "lift-json"                         % liftVersion(scalaVersion.value) % "test, provided",
        "com.storm-enroute"            %% "scalameter"                        % Versions.scalameter             % "test, provided",
        "ch.qos.logback"               % "logback-classic"                    % Versions.logback                % "test, provided"
      )
    ).dependsOn(
      phantomConnectors
    )

  lazy val phantomJdk8 = Project(
    id = "phantom-jdk8",
    base = file("phantom-jdk8"),
    settings = Defaults.coreDefaultSettings ++ sharedSettings
  ).settings(
    name := "phantom-jdk8",
    testOptions in Test += Tests.Argument("-oF"),
    logBuffered in Test := false,
    concurrentRestrictions in Test := Seq(
      Tags.limit(Tags.ForkedTestGroup, defaultConcurrency)
    )
  ).dependsOn(
    phantomDsl % "compile->compile;test->test"
  )

  lazy val phantomConnectors = Project(
    id = "phantom-connectors",
    base = file("phantom-connectors"),
    settings = sharedSettings
  ).configs(PerformanceTest).settings(
    name := "phantom-connectors",
    libraryDependencies ++= Seq(
      "com.datastax.cassandra"       %  "cassandra-driver-core"             % Versions.datastax,
      "com.websudos"                 %% "util-testing"                      % Versions.util            % "test, provided"
    )
  )

  lazy val phantomUdt = Project(
    id = "phantom-udt",
    base = file("phantom-udt"),
    settings = sharedSettings
  ).settings(
    name := "phantom-udt",
    scalacOptions ++= Seq(
      "-language:experimental.macros"
    ),
    libraryDependencies ++= Seq(
      "com.websudos"                 %% "util-testing"                      % Versions.util            % "test, provided"
    )
  ).dependsOn(
    phantomDsl,
    phantomZookeeper
  )

  lazy val phantomThrift = Project(
    id = "phantom-thrift",
    base = file("phantom-thrift"),
    settings = Defaults.coreDefaultSettings ++ sharedSettings ++ ScroogeSBT.newSettings
  ).settings(
    name := "phantom-thrift",
    libraryDependencies ++= Seq(
      "org.slf4j"                    % "slf4j-log4j12"                      % Versions.slf4j % "test, provided",
      "org.apache.thrift"            % "libthrift"                          % Versions.thrift,
      "com.twitter"                  %% "scrooge-core"                      % Versions.scrooge,
      "com.twitter"                  %% "scrooge-serializer"                % Versions.scrooge,
      "com.websudos"                 %% "util-testing"                      % Versions.util               % "test, provided"
    )
  ).dependsOn(
    phantomDsl
  )

  lazy val phantomZookeeper = Project(
    id = "phantom-zookeeper",
    base = file("phantom-zookeeper"),
    settings = sharedSettings
  ).settings(
    name := "phantom-zookeeper",
    libraryDependencies ++= Seq(
      "org.xerial.snappy"            % "snappy-java"      % "1.1.1.3",
      "com.websudos"                 %% "util-testing"    % Versions.util            % "test, provided",
      "com.websudos"                 %% "util-zookeeper"  % Versions.util            % "test, provided" excludeAll ExclusionRule("org.slf4j", "slf4j-jdk14")
    )
  ).dependsOn(
    phantomConnectors
  )

  lazy val phantomReactiveStreams = Project(
    id = "phantom-reactivestreams",
    base = file("phantom-reactivestreams"),
    settings = sharedSettings
  ).settings(
    name := "phantom-reactivestreams",
    libraryDependencies ++= Seq(
      "com.typesafe.play"   %% "play-streams-experimental" % Versions.play exclude("com.typesafe", "config"),
      "com.typesafe"        % "config"                % Versions.typesafeConfig,
      "org.reactivestreams" % "reactive-streams"      % Versions.reactivestreams,
      "com.typesafe.akka"   %% s"akka-actor"          % Versions.akka,
      "com.websudos"        %% "util-testing"         % Versions.util            % "test, provided",
      "org.reactivestreams" % "reactive-streams-tck"  % Versions.reactivestreams % "test, provided"
    )
  ).dependsOn(
    phantomConnectors,
    phantomDsl
  )

  lazy val phantomExample = Project(
    id = "phantom-example",
    base = file("phantom-example"),
    settings = sharedSettings ++ ScroogeSBT.newSettings
  ).settings(
    name := "phantom-example",
    libraryDependencies ++= Seq(
      "com.websudos"                 %% "util-lift"                         % Versions.util            % "test, provided",
      "com.websudos"                 %% "util-testing"                      % Versions.util            % "test, provided"
    )
  ).dependsOn(
    phantomDsl,
    phantomThrift,
    phantomZookeeper
  )

  lazy val phantomContainerTests = Project(
    id = "phantom-container-test",
    base = file("phantom-container-test"),
    settings = sharedSettings
  ).settings(
    name := "phantom-test",
    fork := false,
    logBuffered in Test := false,
    testOptions in Test := Seq(Tests.Filter(s => s.indexOf("IterateeBig") == -1)),
    concurrentRestrictions in Test := Seq(
      Tags.limit(Tags.ForkedTestGroup, defaultConcurrency)
    )
  ).settings(
    libraryDependencies ++= Seq(
      "org.json4s"                %% "json4s-native"                  % Versions.json4s,
      "org.json4s"                %% "json4s-ext"                     % Versions.json4s,
      "net.liftweb"               %% "lift-webkit"                    % liftVersion(scalaVersion.value),
      "net.liftweb"               %% "lift-json"                      % liftVersion(scalaVersion.value),
      "net.databinder.dispatch"   %% "dispatch-core"                  % Versions.dispatch      % "test",
      "javax.servlet"             % "javax.servlet-api"               % "3.0.1"                % "provided",
      "com.websudos"              %% "util-testing"                   % Versions.util          % "provided"
    )
  ).dependsOn(
    phantomDsl,
    phantomThrift,
    phantomZookeeper
  )
}
