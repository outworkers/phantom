import sbt._
import Keys._
import Tests._
import com.twitter.sbt._
import com.typesafe.sbteclipse.plugin.EclipsePlugin._

object newzlyPhantom extends Build {
	val datastaxDriverVersion = "2.0.0-rc1";
	val liftVersion = "3.0-SNAPSHOT";
	val scalatestVersion = "2.0.M8";
	val finagleVersion = "6.7.4";

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


	val publishSettings : Seq[sbt.Project.Setting[_]] = Seq(
		credentials += Credentials(Path.userHome / ".ivy2" / ".credentials"),
		publishMavenStyle := true,
		publishTo := {
  		val nexus = "https://oss.sonatype.org/"
    			Some("releases"  at nexus + "service/local/staging/deploy/maven2")
		},
		publishArtifact in Test := false,
		pomIncludeRepository := { _ => false },
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
		      <id>alexflav</id>
		      <name>Flavian Alexandru</name>
		      <url>http://github.com/alexflav23</url>
		    </developer>
		  </developers>)

	)



	lazy val phantom = Project(
		id = "phantom",
		base = file("."),
        settings = Project.defaultSettings ++ VersionManagement.newSettings ++ sharedSettings
	).aggregate(
		phantomRecord,
		phantomDsl
	)

	lazy val phantomRecord = Project(
		id = "phantom-record",
		base = file("phantom-record"),
		settings = Project.defaultSettings ++ VersionManagement.newSettings ++ sharedSettings ++ publishSettings
	).settings(
		libraryDependencies ++= Seq(
			"org.scalatest"            %% "scalatest"                         % scalatestVersion      % "provided, test", 
			"org.specs2"               %% "specs2-core"                       % "2.3.4"               % "provided, test",
			"net.liftweb"              %% "lift-record"                       % liftVersion           % "compile",
			"com.datastax.cassandra"   %  "cassandra-driver-core"             % datastaxDriverVersion,
			"org.apache.cassandra"     %  "cassandra-all"                     % "2.0.2"               % "compile, test" exclude("org.slf4j", "slf4j-log4j12")
		)
	)

	lazy val phantomDsl = Project(
		id = "phantom-dsl",
		base = file("phantom-dsl"),
		settings = Project.defaultSettings ++ VersionManagement.newSettings ++ sharedSettings ++ publishSettings
	).settings(
		libraryDependencies ++= Seq(
			"org.scalatest"            %% "scalatest"                          % scalatestVersion      % "provided, test",
			"org.specs2"               %% "specs2-core"                        % "2.3.4"               % "provided, test"
		)
	).dependsOn(
		phantomRecord
	)
}