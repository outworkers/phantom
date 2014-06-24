import sbt._
import Keys._
import com.twitter.scrooge.ScroogeSBT
import sbtassembly.Plugin._
import sbtassembly.Plugin.AssemblyKeys._
import scoverage.ScoverageSbtPlugin.instrumentSettings

import org.scoverage.coveralls.CoverallsPlugin.coverallsSettings

object phantom extends Build {

  val newzlyUtilVersion = "0.1.1"
  val datastaxDriverVersion = "2.0.2"
  val scalatestVersion = "2.2.0-M1"
  val finagleVersion = "6.16.0"
  val scroogeVersion = "3.15.0"
  val thriftVersion = "0.9.1"
  val ScalatraVersion = "2.2.2"

  val sharedSettings: Seq[sbt.Project.Setting[_]] = Seq(
    organization := "com.newzly",
    version := "0.8.1",
    scalaVersion := "2.10.4",
    resolvers ++= Seq(
      "Typesafe repository snapshots" at "http://repo.typesafe.com/typesafe/snapshots/",
      "Typesafe repository releases" at "http://repo.typesafe.com/typesafe/releases/",
      "Sonatype repo"                    at "https://oss.sonatype.org/content/groups/scala-tools/",
      "Sonatype releases"                at "https://oss.sonatype.org/content/repositories/releases",
      "Sonatype snapshots"               at "https://oss.sonatype.org/content/repositories/snapshots",
      "Sonatype staging"                 at "http://oss.sonatype.org/content/repositories/staging",
      "Java.net Maven2 Repository"       at "http://download.java.net/maven/2/",
      "Twitter Repository"               at "http://maven.twttr.com",
      "newzly snapshots"                 at "http://maven.newzly.com/repository/snapshots",
      "newzly repository"                at "http://maven.newzly.com/repository/internal"
    ),
    unmanagedSourceDirectories in Compile <<= (scalaSource in Compile)(Seq(_)),
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
     )
  ) ++ net.virtualvoid.sbt.graph.Plugin.graphSettings ++ instrumentSettings

  val mavenPublishSettings : Seq[sbt.Project.Setting[_]] = Seq(
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
      <url>https://github.com/newzly.phantom</url>
        <licenses>
          <license>
            <name>Apache License, Version 2.0</name>
            <url>http://www.apache.org/licenses/LICENSE-2.0.html</url>
            <distribution>repo</distribution>
          </license>
        </licenses>
        <scm>
          <url>git@github.com:newzly/phantom.git</url>
          <connection>scm:git:git@github.com:newzly/phantom.git</connection>
        </scm>
        <developers>
          <developer>
            <id>creyer</id>
            <name>Sorin Chiprian</name>
            <url>http://github.com/creyer</url>
          </developer>
          <developer>
            <id>alexflav</id>
            <name>Flavian Alexandru</name>
            <url>http://github.com/alexflav23</url>
          </developer>
        </developers>
  )

  val publishSettings : Seq[sbt.Project.Setting[_]] = Seq(
      publishTo := Some("newzly releases" at "http://maven.newzly.com/repository/internal"),
      credentials += Credentials(Path.userHome / ".ivy2" / ".credentials"),
      publishMavenStyle := true,
      publishArtifact in Test := false,
      pomIncludeRepository := { _ => true },
      pomExtra := <url>https://github.com/newzly.phantom</url>
        <licenses>
          <license>
            <name>Apache V2 License</name>
            <url>http://www.apache.org/licenses/LICENSE-2.0.html</url>
            <distribution>repo</distribution>
          </license>
        </licenses>
        <scm>
          <url>git@github.com:newzly/phantom.git</url>
          <connection>scm:git:git@github.com:newzly/phantom.git</connection>
        </scm>
        <developers>
          <developer>
            <id>creyer</id>
            <name>Sorin Chiprian</name>
            <url>http://github.com/creyer</url>
          </developer>
          <developer>
            <id>alexflav</id>
            <name>Flavian Alexandru</name>
            <url>http://github.com/alexflav23</url>
          </developer>
        </developers>
    )

  lazy val phantom = Project(
    id = "phantom",
    base = file("."),
    settings = Project.defaultSettings ++ sharedSettings ++ publishSettings ++ coverallsSettings
  ).settings(
    name := "phantom"
  ).aggregate(
    phantomDsl,
    phantomExample,
    phantomScalatraTest,
    phantomThrift
  )

  lazy val phantomDsl = Project(
    id = "phantom-dsl",
    base = file("phantom-dsl"),
    settings = Project.defaultSettings ++
      sharedSettings ++
      publishSettings
  ).settings(
    name := "phantom-dsl",
    fork := true,
    testOptions in Test := Seq(Tests.Filter(s => s.indexOf("IterateeBig") == -1)),
    concurrentRestrictions in Test := Seq(
      Tags.limit(Tags.ForkedTestGroup, 4)
    ),
    libraryDependencies ++= Seq(
      "org.scala-lang"               %  "scala-reflect"                     % "2.10.4",
      "com.twitter"                  %% "util-core"                         % finagleVersion,
      "com.typesafe.play"            %% "play-iteratees"                    % "2.2.0",
      "joda-time"                    %  "joda-time"                         % "2.3",
      "org.joda"                     %  "joda-convert"                      % "1.6",
      "com.datastax.cassandra"       %  "cassandra-driver-core"             % datastaxDriverVersion,
      "org.scalacheck"               %% "scalacheck"                        % "1.11.4"                  % "test, provided",
      "com.newzly"                   %% "util-testing"                      % newzlyUtilVersion         % "provided",
      "com.newzly"                   %% "util-testing-cassandra"            % newzlyUtilVersion         % "provided"
    )
  )

  lazy val phantomThrift = Project(
    id = "phantom-thrift",
    base = file("phantom-thrift"),
    settings = Project.defaultSettings ++
      sharedSettings ++
      publishSettings ++
      ScroogeSBT.newSettings
  ).settings(
    name := "phantom-thrift",
    libraryDependencies ++= Seq(
      "org.apache.thrift"            %  "libthrift"                         % thriftVersion,
      "com.twitter"                  %% "scrooge-core"                      % scroogeVersion,
      "com.twitter"                  %% "scrooge-runtime"                   % scroogeVersion,
      "com.twitter"                  %% "scrooge-serializer"                % scroogeVersion,
      "com.newzly"                   %% "util-testing"                      % newzlyUtilVersion         % "test, provided",
      "com.newzly"                   %% "util-testing-cassandra"            % newzlyUtilVersion         % "provided"
    )
  ).dependsOn(
    phantomDsl
  )

  lazy val phantomExample = Project(
    id = "phantom-example",
    base = file("phantom-example"),
    settings = Project.defaultSettings ++
      sharedSettings ++
      publishSettings ++
      ScroogeSBT.newSettings
  ).settings(
    name := "phantom-example"
  ).dependsOn(
    phantomDsl,
    phantomThrift
  )

  lazy val phantomScalatraTest = Project(
    id = "phantom-scalatra-test",
    base = file("phantom-scalatra-test"),
    settings = Project.defaultSettings ++
      assemblySettings ++
      sharedSettings
  ).settings(
    name := "phantom-test",
    fork := true,
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
      "org.json4s"                %% "json4s-jackson"                   % "3.2.6",
      "org.json4s"                %% "json4s-ext"                       % "3.2.6",
      "net.databinder.dispatch"   %% "dispatch-core"                    % "0.11.0"               % "test",
      "net.databinder.dispatch"   %% "dispatch-json4s-jackson"          % "0.11.0"               % "test",
      "org.eclipse.jetty"         % "jetty-webapp"                      % "8.1.8.v20121106",
      "org.eclipse.jetty.orbit"   % "javax.servlet"                     % "3.0.0.v201112011016"  % "provided;test" artifacts Artifact("javax.servlet", "jar", "jar"),
      "com.newzly"                %% "util-testing"                     % newzlyUtilVersion      % "provided",
      "com.newzly"                %% "util-testing-cassandra"           % newzlyUtilVersion      % "provided"
    )
  ).dependsOn(
    phantomDsl,
    phantomThrift
  )
}
