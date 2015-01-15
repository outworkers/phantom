import com.twitter.scrooge.ScroogeSBT
import org.scoverage.coveralls.CoverallsPlugin.coverallsSettings
import sbt.Keys._
import sbt._
import scoverage.ScoverageSbtPlugin.instrumentSettings

object phantom extends Build {

  val UtilVersion = "0.5.0"
  val DatastaxDriverVersion = "2.1.1"
  val ScalaTestVersion = "2.2.1"
  val FinagleVersion = "6.24.0"
  val TwitterUtilVersion = "6.23.0"
  val ScroogeVersion = "3.17.0"
  val ThriftVersion = "0.9.1"
  val ScalatraVersion = "2.3.0"
  val PlayVersion = "2.4.0-M1"
  val Json4SVersion = "3.2.11"
  val ScalaMeterVersion = "0.6"

  val publishUrl = "http://maven.websudos.co.uk"

  val mavenPublishSettings : Seq[Def.Setting[_]] = Seq(
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
            <name>Apache License, Version 2.0</name>
            <url>http://www.apache.org/licenses/LICENSE-2.0.html</url>
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

  val publishSettings : Seq[Def.Setting[_]] = Seq(
    credentials += Credentials(Path.userHome / ".ivy2" / ".credentials"),
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

  val sharedSettings: Seq[Def.Setting[_]] = Seq(
    organization := "com.websudos",
    version := "1.5.0",
    scalaVersion := "2.11.4",
    crossScalaVersions := Seq("2.10.4", "2.11.4"),
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
    javaOptions in Test ++= Seq("-Xmx2G")
  ) ++ net.virtualvoid.sbt.graph.Plugin.graphSettings ++ instrumentSettings ++ publishSettings


  lazy val phantom = Project(
    id = "phantom",
    base = file("."),
    settings = Defaults.coreDefaultSettings ++ sharedSettings ++ coverallsSettings
  ).settings(
    name := "phantom"
  ).aggregate(
    phantomDsl,
    phantomExample,
    // phantomScalatraTest,
    // phantomSpark,
    phantomTesting,
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
  ).settings(
    name := "phantom-dsl",
    fork := true,
    testOptions in Test += Tests.Argument("-oF"),
    logBuffered in Test := false,
    testOptions in Test := Seq(Tests.Filter(s => s.indexOf("IterateeBig") == -1)),
    testFrameworks += new TestFramework("org.scalameter.ScalaMeterFramework"),
    concurrentRestrictions in Test := Seq(
      Tags.limit(Tags.ForkedTestGroup, 4)
    ),
    libraryDependencies ++= Seq(
      "org.scala-lang"               %  "scala-reflect"                     % scalaVersion.value,
      "com.twitter"                  %% "util-core"                         % TwitterUtilVersion,
      "com.typesafe.play"            %% "play-iteratees"                    % "2.4.0-M1",
      "joda-time"                    %  "joda-time"                         % "2.3",
      "org.joda"                     %  "joda-convert"                      % "1.6",
      "com.datastax.cassandra"       %  "cassandra-driver-core"             % DatastaxDriverVersion,
      "org.scalacheck"               %% "scalacheck"                        % "1.11.5"                  % "test, provided",
      "com.websudos"                 %% "util-testing"                      % UtilVersion               % "test, provided",
      "net.liftweb"                  %% "lift-json"                         % "2.6-M4"                  % "test, provided",
      "com.storm-enroute"            %% "scalameter"                        % ScalaMeterVersion         % "test, provided"
    )
  ).dependsOn(
    phantomTesting % "test, provided"
  )

  lazy val phantomUdt = Project(
    id = "phantom-udt",
    base = file("phantom-udt"),
    settings = Defaults.coreDefaultSettings ++ sharedSettings
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
    phantomTesting % "test, provided"
  )

  /*
  lazy val phantomSpark = Project(
    id = "phantom-spark",
    base = file("phantom-spark"),
    settings = Defaults.coreDefaultSettings ++
      sharedSettings ++ publishSettings
  ).settings(
    name := "phantom-spark",
    libraryDependencies ++= Seq(
      "com.datastax.spark"           %% "spark-cassandra-connector"         % "1.0.0-rc6" exclude("com.datastax.cassandra", "cassandra-driver-core")
    )
  ).dependsOn(
    phantomDsl,
    phantomZookeeper,
    phantomTesting % "test, provided"
  )*/

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
    phantomTesting % "test, provided"
  )

  lazy val phantomZookeeper = Project(
    id = "phantom-zookeeper",
    base = file("phantom-zookeeper"),
    settings = Defaults.coreDefaultSettings ++ sharedSettings
  ).settings(
    name := "phantom-zookeeper",
    libraryDependencies ++= Seq(
      "org.xerial.snappy"            % "snappy-java"                        % "1.1.1.3",
      "org.scalatest"                %% "scalatest"                         % ScalaTestVersion,
      "com.datastax.cassandra"       %  "cassandra-driver-core"             % DatastaxDriverVersion,
      "com.twitter"                  %% "finagle-serversets"                % FinagleVersion exclude("org.slf4j", "slf4j-jdk14"),
      "com.twitter"                  %% "finagle-zookeeper"                 % FinagleVersion,
      "com.websudos"                 %% "util-testing"                      % UtilVersion            % "test, provided",
      "org.cassandraunit"            %  "cassandra-unit"                    % "2.0.2.4"              % "test, provided"  excludeAll(
        ExclusionRule("org.slf4j", "slf4j-log4j12"),
        ExclusionRule("org.slf4j", "slf4j-jdk14")
      )
    )
  )

  lazy val phantomTesting = Project(
    id = "phantom-testing",
    base = file("phantom-testing"),
    settings = Defaults.coreDefaultSettings ++ sharedSettings
  ).settings(
    name := "phantom-testing",
    libraryDependencies ++= Seq(
      "com.twitter"                      %% "util-core"                % TwitterUtilVersion,
      "org.scalatest"                    %% "scalatest"                % ScalaTestVersion,
      "org.scalacheck"                   %% "scalacheck"               % "1.11.3"              % "test",
      "com.twitter"                      %% "finagle-serversets"       % FinagleVersion,
      "com.twitter"                      %% "finagle-zookeeper"        % FinagleVersion,
      "org.cassandraunit"                %  "cassandra-unit"           % "2.0.2.4"  excludeAll (
        ExclusionRule("org.slf4j", "slf4j-log4j12"),
        ExclusionRule("org.slf4j", "slf4j-jdk14")
      )
    )
  ).dependsOn(
    phantomZookeeper
  )

  lazy val phantomExample = Project(
    id = "phantom-example",
    base = file("phantom-example"),
    settings = Defaults.coreDefaultSettings ++ sharedSettings ++ ScroogeSBT.newSettings
  ).settings(
    name := "phantom-example"
  ).dependsOn(
    phantomDsl,
    phantomThrift,
    phantomZookeeper,
    phantomTesting
  )

  lazy val phantomScalatraTest = Project(
    id = "phantom-scalatra-test",
    base = file("phantom-scalatra-test"),
    settings = Defaults.coreDefaultSettings ++ sharedSettings
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
      "org.scalacheck"            %% "scalacheck"                       % "1.11.4",
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
    phantomTesting
  )
}
