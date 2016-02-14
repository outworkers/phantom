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
 * - Explicit consent must be obtained from the copyright owner, Websudos Limited before any redistribution is made.
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

  val UtilVersion = "0.10.8"
  val DatastaxDriverVersion = "3.0.0"
  val ScalaTestVersion = "2.2.4"
  val ShapelessVersion = "2.2.5"
  val FinagleVersion = "6.28.0"
  val TwitterUtilVersion = "6.27.0"
  val ScroogeVersion = "3.17.0"
  val ScalatraVersion = "2.3.0"
  val PlayVersion = "2.4.3"
  val Json4SVersion = "3.2.11"
  val ScalaMeterVersion = "0.6"
  val SparkCassandraVersion = "1.2.0-alpha3"
  val ThriftVersion = "0.5.0"
  val DieselEngineVersion = "0.2.4"
  val Slf4jVersion = "1.7.12"
  val ReactiveStreamsVersion = "1.0.0"
  val AkkaVersion = "2.3.14"
  val TypesafeConfigVersion = "1.2.1"
  val JettyVersion = "9.1.2.v20140210"

  val mavenPublishSettings : Seq[Def.Setting[_]] = Seq(
    credentials += Credentials(Path.userHome / ".ivy2" / ".credentials"),
    publishMavenStyle := true,
    publishTo <<= version.apply {
      v =>
        val nexus = "https://oss.sonatype.org/"
        if (v.trim.endsWith("SNAPSHOT")) {
          Some("snapshots" at nexus + "content/repositories/snapshots")
        } else {
          Some("releases" at nexus + "service/local/staging/deploy/maven2")
        }
    },
    licenses += ("Websudos license", url("https://github.com/websudos/phantom/blob/develop/LICENSE.txt")),
    publishArtifact in Test := false,
    pomIncludeRepository := { _ => true },
    pomExtra :=
      <url>https://github.com/websudos/phantom</url>
        <scm>
          <url>git@github.com:websudos/phantom.git</url>
          <connection>scm:git:git@github.com:websudos/phantom.git</connection>
        </scm>
        <developers>
          <developer>
            <id>alexflav</id>
            <name>Flavian Alexandru</name>
            <url>http://github.com/alexflav23</url>
          </developer>
        </developers>
  )

  def liftVersion(scalaVersion: String): String = {
    scalaVersion match {
      case "2.10.5" => "3.0-M1"
      case _ => "3.0-M6"
    }
  }

  val PerformanceTest = config("perf").extend(Test)
  def performanceFilter(name: String): Boolean = name endsWith "PerformanceTest"

  val publishSettings: Seq[Def.Setting[_]] = Seq(
    publishMavenStyle := true,
    bintray.BintrayKeys.bintrayOrganization := Some("websudos"),
    bintray.BintrayKeys.bintrayRepository <<= scalaVersion.apply {
      v => if (v.trim.endsWith("SNAPSHOT")) "oss-snapshots" else "oss-releases"
    },
    bintray.BintrayKeys.bintrayReleaseOnPublish in ThisBuild := true,
    publishArtifact in Test := false,
    pomIncludeRepository := { _ => true},
    licenses += ("Apache-2.0", url("https://www.apache.org/licenses/LICENSE-2.0"))
  )


  val sharedSettings: Seq[Def.Setting[_]] = Defaults.coreDefaultSettings ++ Seq(
    organization := "com.websudos",
    version := "1.21.5",
    scalaVersion := "2.11.7",
    crossScalaVersions := Seq("2.10.5", "2.11.7"),
    resolvers ++= Seq(
      "Typesafe repository snapshots" at "http://repo.typesafe.com/typesafe/snapshots/",
      "Typesafe repository releases" at "http://repo.typesafe.com/typesafe/releases/",
      "Sonatype repo"                    at "https://oss.sonatype.org/content/groups/scala-tools/",
      "Sonatype releases"                at "https://oss.sonatype.org/content/repositories/releases",
      "Sonatype snapshots"               at "https://oss.sonatype.org/content/repositories/snapshots",
      "Sonatype staging"                 at "http://oss.sonatype.org/content/repositories/staging",
      "Java.net Maven2 Repository"       at "http://download.java.net/maven/2/",
      "Twitter Repository"               at "http://maven.twttr.com",
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
    testFrameworks in PerformanceTest := Seq(new TestFramework("org.scalameter.ScalaMeterFramework")),
    testOptions in Test := Seq(Tests.Filter(x => !performanceFilter(x))),
    testOptions in PerformanceTest := Seq(Tests.Filter(x => performanceFilter(x))),
    fork in PerformanceTest := false,
    parallelExecution in ThisBuild := false
  ) ++ net.virtualvoid.sbt.graph.Plugin.graphSettings ++ publishSettings ++ VersionManagement.newSettings

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
    phantomDsl,
    phantomExample,
    phantomConnectors,
    phantomReactiveStreams,
    phantomThrift,
    phantomUdt,
    phantomZookeeper
  )

  lazy val phantomDsl = Project(
    id = "phantom-dsl",
    base = file("phantom-dsl"),
    settings = Defaults.coreDefaultSettings ++
      sharedSettings ++
      publishSettings
  ).configs(
      PerformanceTest
    ).settings(
      inConfig(PerformanceTest)(Defaults.testTasks): _*
    ).settings(
      name := "phantom-dsl",
      testOptions in Test += Tests.Argument("-oF"),
      logBuffered in Test := false,
      concurrentRestrictions in Test := Seq(
        Tags.limit(Tags.ForkedTestGroup, 4)
      ),
      libraryDependencies ++= Seq(
        "org.scala-lang"               %  "scala-reflect"                     % scalaVersion.value,
        "com.websudos"                 %% "diesel-engine"                     % DieselEngineVersion,
        "com.chuusai"                  %% "shapeless"                         % ShapelessVersion,
        "com.twitter"                  %% "util-core"                         % TwitterUtilVersion,
        "com.typesafe.play"            %% "play-iteratees"                    % "2.4.0-M1" exclude ("com.typesafe", "config"),
        "joda-time"                    %  "joda-time"                         % "2.3",
        "org.joda"                     %  "joda-convert"                      % "1.6",
        "com.datastax.cassandra"       %  "cassandra-driver-core"             % DatastaxDriverVersion,
        "com.datastax.cassandra"       %  "cassandra-driver-extras"           % DatastaxDriverVersion,
        "org.slf4j"                    % "log4j-over-slf4j"                   % "1.7.12",
        "org.scalacheck"               %% "scalacheck"                        % "1.11.5"                        % "test, provided",
        "com.websudos"                 %% "util-lift"                         % UtilVersion                     % "test, provided",
        "com.websudos"                 %% "util-testing"                      % UtilVersion                     % "test, provided",
        "net.liftweb"                  %% "lift-json"                         % liftVersion(scalaVersion.value) % "test, provided",
        "com.storm-enroute"            %% "scalameter"                        % ScalaMeterVersion               % "test, provided"
      )
    ).dependsOn(
      phantomConnectors
    )

  lazy val phantomConnectors = Project(
    id = "phantom-connectors",
    base = file("phantom-connectors"),
    settings = sharedSettings
  ).configs(PerformanceTest).settings(
    name := "phantom-connectors",
    libraryDependencies ++= Seq(
      "com.datastax.cassandra"       %  "cassandra-driver-core"             % DatastaxDriverVersion,
      "com.websudos"                 %% "util-testing"                      % UtilVersion            % "test, provided"
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
      "com.websudos"                 %% "util-testing"                      % UtilVersion            % "test, provided"
    )
  ).dependsOn(
    phantomDsl,
    phantomZookeeper
  )

  lazy val phantomThrift = Project(
    id = "phantom-thrift",
    base = file("phantom-thrift"),
    settings = Defaults.coreDefaultSettings ++
      sharedSettings ++
      publishSettings ++
      ScroogeSBT.newSettings
  ).settings(
    name := "phantom-thrift",
    libraryDependencies ++= Seq(
      "org.slf4j"                    % "slf4j-log4j12"                      % Slf4jVersion % "test, provided",
      "org.apache.thrift"            % "libthrift"                          % ThriftVersion,
      "com.twitter"                  %% "scrooge-core"                      % ScroogeVersion,
      "com.twitter"                  %% "scrooge-serializer"                % ScroogeVersion,
      "com.websudos"                 %% "util-testing"                      % UtilVersion               % "test, provided"
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
      "org.xerial.snappy"            % "snappy-java"                        % "1.1.1.3",
      "com.websudos"                 %% "util-testing"                      % UtilVersion            % "test, provided",
      "com.websudos"                 %% "util-zookeeper"                    % UtilVersion            % "test, provided" excludeAll ExclusionRule("org.slf4j", "slf4j-jdk14")
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
      "com.typesafe.play"   %% "play-streams-experimental" % PlayVersion exclude("com.typesafe", "config"),
      "com.typesafe"        % "config"                % TypesafeConfigVersion,
      "org.reactivestreams" % "reactive-streams"      % ReactiveStreamsVersion,
      "com.typesafe.akka"   %% s"akka-actor"          % AkkaVersion,
      "com.websudos"        %% "util-testing"         % UtilVersion            % "test, provided",
      "org.reactivestreams" % "reactive-streams-tck"  % ReactiveStreamsVersion % "test, provided"
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
      "com.websudos"                 %% "util-lift"                         % UtilVersion            % "test, provided",
      "com.websudos"                 %% "util-testing"                      % UtilVersion            % "test, provided"
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
      Tags.limit(Tags.ForkedTestGroup, 4)
    )
  ).settings(
    libraryDependencies ++= Seq(
      "net.liftweb"               %% "lift-webkit"                    % liftVersion(scalaVersion.value),
      "net.liftweb"               %% "lift-json"                      % liftVersion(scalaVersion.value),
      "net.databinder.dispatch"   %% "dispatch-core"                  % "0.11.0"               % "test",
      "javax.servlet"             % "javax.servlet-api"               % "3.0.1"                % "provided",
      "com.websudos"              %% "util-testing"                   % UtilVersion            % "provided"
    )
  ).dependsOn(
    phantomDsl,
    phantomThrift,
    phantomZookeeper
  )
}
