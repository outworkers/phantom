import sbt._
import Keys._
import sbtassembly.Plugin.AssemblyKeys._
import scala.Some
import Tests._
import com.twitter.sbt._
import com.twitter.scrooge.ScroogeSBT
import sbtassembly.Plugin._

object newzlyPhantom extends Build {

  val datastaxDriverVersion = "2.0.0-rc2"
  val liftVersion = "2.6-M2"
  val scalatestVersion = "2.0.M8"
  val finagleVersion = "6.8.1"
  val scroogeVersion = "3.11.2"

  val thriftLibs = Seq(
    "org.apache.thrift" % "libthrift" % "0.9.1" intransitive()
  )
  val scroogeLibs = thriftLibs ++ Seq(
    "com.twitter" %% "scrooge-runtime" % scroogeVersion
  )

  val sharedSettings: Seq[sbt.Project.Setting[_]] = Seq(
       organization := "com.newzly",
       version := "0.0.5",
       scalaVersion := "2.10.0",
       resolvers ++= Seq(
        "Sonatype repo"                    at "https://oss.sonatype.org/content/groups/scala-tools/",
        "Sonatype releases"                at "https://oss.sonatype.org/content/repositories/releases",
        "Sonatype snapshots"               at "https://oss.sonatype.org/content/repositories/snapshots",
        "Sonatype staging"                 at "http://oss.sonatype.org/content/repositories/staging",
        "Java.net Maven2 Repository"       at "http://download.java.net/maven/2/",
        "Twitter Repository"               at "http://maven.twttr.com"
       ),
       libraryDependencies ++= Seq(
         "com.github.nscala-time"  %% "nscala-time"                       % "0.4.2"
       ),
       unmanagedSourceDirectories in Compile <<= (scalaSource in Compile)(Seq(_)),
       scalacOptions ++= Seq(
           "-language:postfixOps",
           "-language:implicitConversions",
           "-language:reflectiveCalls",
           "-language:higherKinds",
           "-deprecation",
           "-feature",
           "-unchecked"
       )
    ) ++ net.virtualvoid.sbt.graph.Plugin.graphSettings


    val publishSettings : Seq[sbt.Project.Setting[_]] = Seq(
        publishTo := Some("newzly releases" at "http://maven.newzly.com/repository/internal"),
        credentials += Credentials(
          "Repository Archiva Managed internal Repository",
          "maven.newzly.com",
          "admin",
          "newzlymaven2323!"
        ),
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

  lazy val phantomUtil = Project(
    id = "phantom-util",
    base = file("phantom-test"),
    settings = Project.defaultSettings ++ assemblySettings ++ VersionManagement.newSettings ++ sharedSettings
  ).settings(
    name := "phantom-util",
    jarName in assembly := "cassandra.jar",
    outputPath in assembly := file("cassandra.jar"),
    test in assembly := {},
    fork in run := true,
    assemblyOption in assembly ~= {  _.copy(includeScala = true) } ,
    excludedJars in assembly <<= (fullClasspath in assembly) map { cp =>
      cp filter { x => println(":::: "+x)
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
      "org.cassandraunit"        %  "cassandra-unit"                    % "2.0.2.0"
    )
  )


  lazy val phantom = Project(
        id = "phantom",
        base = file("."),
        settings = Project.defaultSettings ++ VersionManagement.newSettings ++ sharedSettings ++ publishSettings
    ).aggregate(
        phantomDsl,
        phantomTest
    )

    lazy val phantomDsl = Project(
        id = "phantom-dsl",
        base = file("phantom-dsl"),
        settings = Project.defaultSettings ++
          VersionManagement.newSettings ++
          sharedSettings ++
          publishSettings ++
          ScroogeSBT.newSettings
    ).settings(
        libraryDependencies ++= Seq(
          "com.twitter"                  %% "util-collection"                   % "6.3.6",
          "com.twitter"                  %% "scrooge-core"                      % scroogeVersion,
          "com.twitter"                  %% "scrooge-runtime"                   % scroogeVersion,
          "com.twitter"                  %% "scrooge-serializer"                % scroogeVersion,
          "com.fasterxml.jackson.module" %% "jackson-module-scala"              % "2.3.1",
          "com.datastax.cassandra"       %  "cassandra-driver-core"             % datastaxDriverVersion,
          "org.apache.cassandra"         %  "cassandra-all"                     % "2.0.2"               % "compile, test" exclude("org.slf4j", "slf4j-log4j12"),
          "org.scala-lang"               %  "scala-reflect"                     % "2.10.0",
          "org.apache.thrift"            % "libthrift"                          % "0.9.1"
        )
    )

  lazy val phantomTest = Project(
        id = "phantom-test",
        base = file("phantom-test"),
        settings = Project.defaultSettings ++ assemblySettings ++ VersionManagement.newSettings ++ sharedSettings ++ publishSettings
    ).settings(
      fork := true,
      concurrentRestrictions in Test := Seq(
        Tags.limit(Tags.ForkedTestGroup, 4)
      )
    ).settings(
        libraryDependencies ++= Seq(
            "org.cassandraunit"        %  "cassandra-unit"                    % "2.0.2.0"             exclude("org.apache.cassandra","cassandra-all"),
            "org.scalatest"            %% "scalatest"                         % scalatestVersion      % "provided, test",
            "org.specs2"               %% "specs2-core"                       % "2.3.4"               % "provided, test"
        )
    ).dependsOn(
        phantomDsl
    )
}
