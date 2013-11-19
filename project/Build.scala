import sbt._
import Keys._
import Tests._
import com.twitter.sbt._
import com.typesafe.sbteclipse.plugin.EclipsePlugin._

object LiftCassandra extends Build {
	val datastaxDriverVersion = "2.0.0-rc1";
	val liftVersion = "3.0-SNAPSHOT";
	val scalatestVersion = "2.0.M8";

    val plugins: Seq[sbt.Project.Setting[_]] = net.virtualvoid.sbt.graph.Plugin.graphSettings

    val sharedSettings: Seq[sbt.Project.Setting[_]] = Seq(
       organization := "com.newzly",
       version := "0.0.1",
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
       unmanagedResourceDirectories in Test <+= (baseDirectory) { _ / "src/main/webapp" },
       scalacOptions ++= Seq(
           "-language:postfixOps",
           "-language:implicitConversions",
           "-deprecation",
           "-feature",
           "-unchecked"
       ),
       EclipseKeys.withSource := true,
       EclipseKeys.createSrc := EclipseCreateSrc.Default + EclipseCreateSrc.Resource
    ) ++ plugins

	lazy val liftCassandra = Project(
		id = "lift-cassandra",
		base = file("."),
        settings = Project.defaultSettings ++ VersionManagement.newSettings
	).aggregate(
		liftCassandraRecord,
		liftCassandraRogue
	)

	lazy val liftCassandraRecord = Project(
		id = "lift-cassandra-record",
		base = file("cassandra-record"),
		settings = Project.defaultSettings ++ VersionManagement.newSettings
	).settings(
		libraryDependencies ++= Seq(
			"org.scalatest"            %% "scalatest"                          % scalatestVersion      % "provided", 
			"net.liftweb"              %%  "lift-record"                       % liftVersion           % "compile",
			"com.datastax.cassandra"   %   "cassandra-driver-core"             % datastaxDriverVersion
		)
	)

	lazy val liftCassandraRogue = Project(
		id = "lift-cassandra-rogue",
		base = file("cassandra-rogue"),
		settings = Project.defaultSettings ++ VersionManagement.newSettings
	).settings(
		libraryDependencies ++= Seq(
			"org.scalatest"            %% "scalatest"                          % scalatestVersion      % "provided"
		)
	).dependsOn(
		liftCassandraRecord
	)
}