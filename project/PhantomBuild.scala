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
import com.twitter.scrooge.ScroogeSBT
import sbt.Keys._
import sbt._

object PhantomBuild extends Build {

  val UtilVersion = "0.7.5"
  val DatastaxDriverVersion = "2.1.5"
  val ScalaTestVersion = "2.2.1"
  val ShapelessVersion = "2.2.0-RC4"
  val FinagleVersion = "6.25.0"
  val TwitterUtilVersion = "6.24.0"
  val ScroogeVersion = "3.17.0"
  val ThriftVersion = "0.9.1"
  val ScalatraVersion = "2.3.0"
  val PlayVersion = "2.4.0-M1"
  val Json4SVersion = "3.2.11"
  val ScalaMeterVersion = "0.6"
  val CassandraUnitVersion = "2.0.2.5"
  val SparkCassandraVersion = "1.2.0-alpha3"

  val publishUrl = "http://maven.websudos.co.uk"

  val mavnePublishSettings : Seq[Def.Setting[_]] = Seq(
    credentials += Credentials(Path.userHome / ".ivy2" / ".credentials"),
    publishMavenStyle := true,
    publishTo <<= version.apply {
      v =>
        val nexus = "https://oss.sonatype.org/"
        if (v.trim.endsWith("SNAPSHOT"))
          Some("snapshots" at nexus + "content/repositories/snapshots")
        else
          Some("releases" at nexus + "service/local/staging/deploy/maven2")
    },
    publishArtifact in Test := false,
    pomIncludeRepository := { _ => true },
    pomExtra :=
      <url>https://github.com/websudos/phantom</url>
        <licenses>
          <license>
            <name>Websudos License</name>
            <url>http://websudos.com/license</url>
            <distribution>repo</distribution>
          </license>
        </licenses>
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
      case _ => "3.0-M2"
    }
  }

  val publishSettings : Seq[Def.Setting[_]] = Seq(
    credentials ++= Seq(
      Credentials(Path.userHome / ".ivy2" / ".credentials")
    ),
    publishTo <<= version { (v: String) => {
        if (v.trim.endsWith("SNAPSHOT"))
          Some("snapshots" at publishUrl + "/ext-snapshot-local")
        else
          Some("releases"  at publishUrl + "/ext-release-local")
      }
    },
    publishMavenStyle := true,
    publishArtifact in Test := false,
    pomIncludeRepository := { _ => true }
  )

  val PerformanceTest = config("perf").extend(Test)
  def performanceFilter(name: String): Boolean = name endsWith "PerformanceTest"

  val bintrayPublishing: Seq[Def.Setting[_]] = Seq(
    publishMavenStyle := false,
    bintray.BintrayKeys.bintrayOrganization := Some("websudos"),
    bintray.BintrayKeys.bintrayRepository := "oss-releases",
    publishArtifact in Test := false,
    pomIncludeRepository := { _ => true},
    licenses += ("Apache-2.0", url("https://www.apache.org/licenses/LICENSE-2.0"))
  )


  val sharedSettings: Seq[Def.Setting[_]] = Defaults.coreDefaultSettings ++ Seq(
    organization := "com.websudos",
    version := "1.8.2",
    scalaVersion := "2.11.6",
    crossScalaVersions := Seq("2.10.5", "2.11.6"),
    resolvers ++= Seq(
      "Typesafe repository snapshots" at "http://repo.typesafe.com/typesafe/snapshots/",
      "Typesafe repository releases" at "http://repo.typesafe.com/typesafe/releases/",
      "Sonatype repo"                    at "https://oss.sonatype.org/content/groups/scala-tools/",
      "Sonatype releases"                at "https://oss.sonatype.org/content/repositories/releases",
      "Sonatype snapshots"               at "https://oss.sonatype.org/content/repositories/snapshots",
      "Sonatype staging"                 at "http://oss.sonatype.org/content/repositories/staging",
      "Java.net Maven2 Repository"       at "http://download.java.net/maven/2/",
      "Twitter Repository"               at "http://maven.twttr.com",
      "Websudos releases"                at "http://maven.websudos.co.uk/ext-release-local",
      "Websudos snapshots"               at "http://maven.websudos.co.uk/ext-snapshot-local"
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
    fork in Test := true,
    javaOptions in Test ++= Seq("-Xmx2G"),
    testFrameworks in PerformanceTest := Seq(new TestFramework("org.scalameter.ScalaMeterFramework")),
    testOptions in Test := Seq(Tests.Filter(x => !performanceFilter(x))),
    testOptions in PerformanceTest := Seq(Tests.Filter(x => performanceFilter(x))),
    fork in PerformanceTest := true
  ) ++ net.virtualvoid.sbt.graph.Plugin.graphSettings ++ publishSettings

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
    // phantomScalatraTest,
    phantomSpark,
    phantomTestKit,
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
      fork := true,
      testOptions in Test += Tests.Argument("-oF"),
      logBuffered in Test := false,
      concurrentRestrictions in Test := Seq(
        Tags.limit(Tags.ForkedTestGroup, 4)
      ),
      libraryDependencies ++= Seq(
        "org.scala-lang"               %  "scala-reflect"                     % scalaVersion.value,
        "com.chuusai"                  %% "shapeless"                         % ShapelessVersion,
        "com.twitter"                  %% "util-core"                         % TwitterUtilVersion,
        "com.typesafe.play"            %% "play-iteratees"                    % "2.4.0-M1",
        "joda-time"                    %  "joda-time"                         % "2.3",
        "org.joda"                     %  "joda-convert"                      % "1.6",
        "com.datastax.cassandra"       %  "cassandra-driver-core"             % DatastaxDriverVersion,
        "org.scalacheck"               %% "scalacheck"                        % "1.11.5"                        % "test, provided",
        "com.websudos"                 %% "util-testing"                      % UtilVersion                     % "test, provided",
        "net.liftweb"                  %% "lift-json"                         % liftVersion(scalaVersion.value) % "test, provided",
        "com.storm-enroute"            %% "scalameter"                        % ScalaMeterVersion               % "test, provided"
      )
    ).dependsOn(
      phantomTestKit % "test, provided",
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
    phantomZookeeper,
    phantomTestKit % "test, provided"
  )

  lazy val phantomSpark = Project(
    id = "phantom-spark",
    base = file("phantom-spark"),
    settings = Defaults.coreDefaultSettings ++
      sharedSettings ++ publishSettings
  ).settings(
    name := "phantom-spark",
    libraryDependencies ++= Seq(
      "com.datastax.spark"           %% "spark-cassandra-connector"         % SparkCassandraVersion
    )
  ).dependsOn(
    phantomDsl,
    phantomZookeeper,
    phantomTestKit % "test, provided"
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
      "org.apache.thrift"            %  "libthrift"                         % ThriftVersion,
      "com.twitter"                  %% "scrooge-core"                      % ScroogeVersion,
      "com.twitter"                  %% "scrooge-serializer"                % ScroogeVersion,
      "org.scalatest"                %% "scalatest"                         % ScalaTestVersion          % "test, provided",
      "com.websudos"                 %% "util-testing"                      % UtilVersion               % "test, provided"
    )
  ).dependsOn(
    phantomDsl,
    phantomTestKit % "test, provided"
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
      "com.websudos"                 %% "util-zookeeper"                    % UtilVersion            % "test, provided" excludeAll ExclusionRule("org.slf4j", "slf4j-jdk14"),
      "org.cassandraunit"            %  "cassandra-unit"                    % CassandraUnitVersion   % "test, provided"  excludeAll(
        ExclusionRule("org.slf4j", "slf4j-log4j12"),
        ExclusionRule("org.slf4j", "slf4j-jdk14")
      )
    )
  ).dependsOn(
    phantomConnectors
  )

  lazy val phantomTestKit = Project(
    id = "phantom-testkit",
    base = file("phantom-testkit"),
    settings = sharedSettings
  ).settings(
    name := "phantom-testkit",
    libraryDependencies ++= Seq(
      "com.twitter"                      %% "util-core"                % TwitterUtilVersion,
      "com.websudos"                     %% "util-zookeeper"           % UtilVersion excludeAll ExclusionRule("org.slf4j", "slf4j-jdk14"),
      "com.websudos"                     %% "util-testing"             % UtilVersion,
      "org.cassandraunit"                %  "cassandra-unit"           % CassandraUnitVersion  excludeAll (
        ExclusionRule("org.slf4j", "slf4j-log4j12"),
        ExclusionRule("org.slf4j", "slf4j-jdk14"),
        ExclusionRule("com.google.guava", "guava")
      )
    )
  ).dependsOn(
    phantomZookeeper
  )

  lazy val phantomExample = Project(
    id = "phantom-example",
    base = file("phantom-example"),
    settings = sharedSettings ++ ScroogeSBT.newSettings
  ).settings(
    name := "phantom-example"
  ).dependsOn(
    phantomDsl,
    phantomThrift,
    phantomZookeeper,
    phantomTestKit
  )

  lazy val phantomScalatraTest = Project(
    id = "phantom-scalatra-test",
    base = file("phantom-scalatra-test"),
    settings = sharedSettings
  ).settings(
    name := "phantom-test",
    fork := true,
    logBuffered in Test := false,
    testOptions in Test := Seq(Tests.Filter(s => s.indexOf("IterateeBig") == -1)),
    concurrentRestrictions in Test := Seq(
      Tags.limit(Tags.ForkedTestGroup, 4)
    )
  ).settings(
    libraryDependencies ++= Seq(
      "org.scalatra"              %% "scalatra"                         % ScalatraVersion,
      "org.scalatra"              %% "scalatra-scalate"                 % ScalatraVersion,
      "org.scalatra"              %% "scalatra-json"                    % ScalatraVersion,
      "org.scalatra"              %% "scalatra-specs2"                  % ScalatraVersion        % "test",
      "org.json4s"                %% "json4s-jackson"                   % Json4SVersion,
      "org.json4s"                %% "json4s-ext"                       % Json4SVersion,
      "net.databinder.dispatch"   %% "dispatch-core"                    % "0.11.0"               % "test",
      "net.databinder.dispatch"   %% "dispatch-json4s-jackson"          % "0.11.0"               % "test",
      "org.eclipse.jetty"         % "jetty-webapp"                      % "8.1.8.v20121106",
      "org.eclipse.jetty.orbit"   % "javax.servlet"                     % "3.0.0.v201112011016"  % "provided;test" artifacts Artifact("javax.servlet", "jar", "jar"),
      "com.websudos"              %% "util-testing"                     % UtilVersion            % "provided"
    )
  ).dependsOn(
    phantomDsl,
    phantomThrift,
    phantomZookeeper,
    phantomTestKit
  )
}
