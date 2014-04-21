import sbt._
import Keys._
import com.twitter.sbt._
import com.twitter.scrooge.ScroogeSBT
import sbtassembly.Plugin._
import sbtassembly.Plugin.AssemblyKeys._

object phantom extends Build {

  val newzlyUtilVersion = "0.0.21"
  val datastaxDriverVersion = "2.0.1"
  val scalatestVersion = "2.1.0"
  val finagleVersion = "6.10.0"
  val scroogeVersion = "3.11.2"
  val thriftVersion = "0.9.1"

  val thriftLibs = Seq(
    "org.apache.thrift" % "libthrift" % thriftVersion intransitive()
  )
  val scroogeLibs = thriftLibs ++ Seq(
    "com.twitter" %% "scrooge-runtime" % scroogeVersion
  )

  val sharedSettings: Seq[sbt.Project.Setting[_]] = Seq(
    organization := "com.newzly",
    version := "0.3.5",
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
  ) ++ net.virtualvoid.sbt.graph.Plugin.graphSettings

  val publishSettings : Seq[sbt.Project.Setting[_]] = Seq(
    credentials += Credentials(Path.userHome / ".ivy2" / ".credentials"),
    publishMavenStyle := true,
    publishTo <<= version.apply{
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
            <name>BSD-style</name>
            <url>http://www.opensource.org/licenses/bsd-license.php</url>
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

  val MavenpublishSettings : Seq[sbt.Project.Setting[_]] = Seq(
      publishTo := Some("newzly releases" at "http://maven.newzly.com/repository/internal"),
      credentials += Credentials(Path.userHome / ".ivy2" / ".credentials"),
      publishMavenStyle := true,
      publishArtifact in Test := false,
      pomIncludeRepository := { _ => true },
      pomExtra := <url>https://github.com/newzly.phantom</url>
        <licenses>
          <license>
            <name>BSD-style</name>
            <url>http://www.opensource.org/licenses/bsd-license.php</url>
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
    settings = Project.defaultSettings ++ VersionManagement.newSettings ++ sharedSettings ++ publishSettings
  ).settings(
    name := "phantom"
  ).aggregate(
    phantomDsl,
    phantomCassandraUnit,
    phantomFinagle,
    phantomThrift,
    phantomTest
  )

  lazy val phantomDsl = Project(
    id = "phantom-dsl",
    base = file("phantom-dsl"),
    settings = Project.defaultSettings ++
      VersionManagement.newSettings ++
      sharedSettings ++
      publishSettings
  ).settings(
    name := "phantom-dsl",
    libraryDependencies ++= Seq(
      "com.twitter"                  %% "util-core"                         % finagleVersion,
      "com.typesafe.play"            %% "play-iteratees"                    % "2.2.0",
      "joda-time"                    %  "joda-time"                         % "2.3",
      "org.joda"                     %  "joda-convert"                      % "1.6",
      "com.datastax.cassandra"       %  "cassandra-driver-core"             % datastaxDriverVersion exclude("log4j", "log4j")

    )
  )

  lazy val phantomCassandraUnit = Project(
    id = "phantom-cassandra-unit",
    base = file("phantom-cassandra-unit"),
    settings = Project.defaultSettings ++
      assemblySettings ++
      VersionManagement.newSettings ++
      sharedSettings ++ publishSettings
  ).settings(
    name := "phantom-cassandra-unit",
    jarName in assembly := "cassandra.jar",
    outputPath in assembly := file("cassandra.jar"),
    test in assembly := {},
    fork in run := true,
    assemblyOption in assembly ~= {  _.copy(includeScala = true) } ,
    excludedJars in assembly <<= (fullClasspath in assembly) map { cp =>
      cp filter { x =>
        x.data.getName.indexOf("specs2_2.") >= 0 ||
          x.data.getName.indexOf("scalap-2.") >= 0 ||
          x.data.getName.indexOf("scala-compiler.jar") >= 0 ||
          x.data.getName.indexOf("scala-json_") >= 0 ||
          x.data.getName.indexOf("netty-3.2.9") >= 0 ||
          x.data.getName.indexOf("com.twitter") >= 0
      }
    }
  ).settings(
    libraryDependencies ++= Seq(
      "org.cassandraunit"        %  "cassandra-unit"                    % "2.0.2.1"
    )
  )

  lazy val phantomThrift = Project(
    id = "phantom-thrift",
    base = file("phantom-thrift"),
    settings = Project.defaultSettings ++
      VersionManagement.newSettings ++
      sharedSettings ++
      publishSettings ++
      ScroogeSBT.newSettings
  ).settings(
    name := "phantom-thrift",
    libraryDependencies ++= Seq(
      "org.apache.thrift"            %  "libthrift"                         % thriftVersion,
      "com.twitter"                  %% "scrooge-core"                      % scroogeVersion,
      "com.twitter"                  %% "scrooge-runtime"                   % scroogeVersion,
      "com.twitter"                  %% "scrooge-serializer"                % scroogeVersion
    )
  ).dependsOn(
    phantomDsl
  )

  lazy val phantomFinagle = Project(
    id = "phantom-finagle",
    base = file("phantom-finagle"),
    settings = Project.defaultSettings ++
      VersionManagement.newSettings ++
      sharedSettings ++
      publishSettings
  ).settings(
    name := "phantom-finagle",
    libraryDependencies ++= Seq(
      "com.twitter"                  %% "util-collection"                   % finagleVersion
    )
  ).dependsOn(
    phantomDsl
  )

  lazy val phantomExample = Project(
    id = "phantom-example",
    base = file("phantom-example"),
    settings = Project.defaultSettings ++
      VersionManagement.newSettings ++
      sharedSettings ++
      publishSettings ++
      ScroogeSBT.newSettings
  ).settings(
    name := "phantom-example"
  ).dependsOn(
    phantomDsl,
    phantomFinagle,
    phantomThrift
  )

  lazy val phantomTest = Project(
    id = "phantom-test",
    base = file("phantom-test"),
    settings = Project.defaultSettings ++
      assemblySettings ++
      VersionManagement.newSettings ++
      sharedSettings ++
      publishSettings
  ).settings(
    name := "phantom-test",
    fork := true,
    testOptions in Test := Seq(Tests.Filter(s => s.indexOf("IterateeBig") == -1)),
    concurrentRestrictions in Test := Seq(
      Tags.limit(Tags.ForkedTestGroup, 4)
    )
  ).settings(
    libraryDependencies ++= Seq(
      "com.newzly"               %% "util-testing"                      % newzlyUtilVersion     % "provided",
      "com.newzly"               %% "util-finagle"                      % newzlyUtilVersion     % "provided",
      "org.scalatest"            %% "scalatest"                         % scalatestVersion      % "provided, test"
    )
  ).dependsOn(
    phantomDsl,
    phantomCassandraUnit,
    phantomFinagle,
    phantomThrift
  )
}
