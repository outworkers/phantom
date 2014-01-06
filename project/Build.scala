import sbt._
import Keys._
import Tests._
import com.twitter.sbt._
import ScctPlugin.instrumentSettings
import ScctPlugin.mergeReportSettings
import com.github.theon.coveralls.CoverallsPlugin.coverallsSettings

object newzlyPhantom extends Build {

  val datastaxDriverVersion = "2.0.0-rc2"
  val liftVersion = "2.6-M2"
  val scalatestVersion = "2.0.M8"
  val finagleVersion = "6.7.4"

  val sharedSettings: Seq[sbt.Project.Setting[_]] = Seq(
       organization := "com.newzly",
       version := "0.0.3",
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
    ) ++ net.virtualvoid.sbt.graph.Plugin.graphSettings ++ coverallsSettings


    val publishSettings : Seq[sbt.Project.Setting[_]] = Seq(
        publishTo := Some("newzly releases" at "http://maven.newzly.com/repository/internal"),
        credentials += Credentials(
          "newzly Maven Repository",
          "maven.newzly.com",
          "admin@newzly.com",
          "newzlymaven2323!"
        ),
        publishMavenStyle := true,
        publishArtifact in Test := false,
        pomIncludeRepository := { _ => true },
        pomExtra := (
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
            
          </developers>)

    )

    lazy val phantom = Project(
        id = "phantom",
        base = file("."),
        settings = Project.defaultSettings ++ VersionManagement.newSettings ++ sharedSettings ++ publishSettings ++ mergeReportSettings
    ).aggregate(
        phantomDsl,
        phantomTest
    )

    def groupByFirst(tests: Seq[TestDefinition]) =
      tests map {t=> new Tests.Group(t.name, Seq(t)  , Tests.SubProcess(Seq.empty))}

    lazy val phantomDsl = Project(
        id = "phantom-dsl",
        base = file("phantom-dsl"),
        settings = Project.defaultSettings ++ VersionManagement.newSettings ++ sharedSettings ++ publishSettings ++ instrumentSettings
    ).settings(
        libraryDependencies ++= Seq(
            "net.liftweb"              %% "lift-json"                         % liftVersion           % "compile, test",
            "com.datastax.cassandra"   %  "cassandra-driver-core"             % datastaxDriverVersion % "compile, test",
            "org.apache.cassandra"     %  "cassandra-all"                     % "2.0.2"               % "compile, test" exclude("org.slf4j", "slf4j-log4j12")
        )
    )

  lazy val phantomTest = Project(
        id = "phantom-test",
        base = file("phantom-test"),
        settings = Project.defaultSettings ++ VersionManagement.newSettings ++ sharedSettings ++ publishSettings
    ).settings(
      fork := true,
      testGrouping <<=  (definedTests in Test map groupByFirst),
      concurrentRestrictions in Test := Seq(
        Tags.limit(Tags.ForkedTestGroup, 1)
      )
    ).settings(
        libraryDependencies ++= Seq(
            "org.cassandraunit"        %  "cassandra-unit"                    % "2.0.2.0"             % "test, provided" exclude("org.apache.cassandra","cassandra-all"),
            "org.scalatest"            %% "scalatest"                         % scalatestVersion      % "provided, test",
            "org.specs2"               %% "specs2-core"                       % "2.3.4"               % "provided, test"
        )
    ) settings (ScctPlugin.instrumentSettings: _*) dependsOn(
        phantomDsl
    )

}
