/*
 * Copyright 2013 - 2017 Outworkers Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import Publishing.{ciSkipSequence, commitTutFiles, releaseTutFolder}
import sbt.Keys._
import sbt._
import sbtrelease.ReleaseStateTransformations._

lazy val ScalacOptions = Seq(
  "-deprecation", // Emit warning and location for usages of deprecated APIs.
  "-encoding",
  "utf-8", // Specify character encoding used by source files.
  "-feature",
  "-explaintypes", // Explain type errors in more detail.
  "-feature", // Emit warning and location for usages of features that should be imported explicitly.
  "-language:reflectiveCalls",
  "-language:postfixOps",
  "-language:existentials", // Existential types (besides wildcard types) can be written and inferred
  "-language:experimental.macros", // Allow macro definition (besides implementation and application)
  "-language:higherKinds", // Allow higher-kinded types
  "-language:implicitConversions", // Allow definition of implicit functions called views
  "-unchecked", // Enable additional warnings where generated code depends on assumptions.
  "-Xcheckinit", // Wrap field accessors to throw an exception on uninitialized access.
  //"-Xfatal-warnings", // Fail the compilation if there are any warnings.
  "-Xfuture" // Turn on future language features.
  //"-Yno-adapted-args" // Do not adapt an argument list (either by inserting () or creating a tuple) to match the receiver.
)

val XLintOptions = Seq(
  "-Xlint:adapted-args", // Warn if an argument list is modified to match the receiver.
  "-Xlint:by-name-right-associative", // By-name parameter of right associative operator.
  "-Xlint:constant", // Evaluation of a constant arithmetic expression results in an error.
  "-Xlint:delayedinit-select", // Selecting member of DelayedInit.
  "-Xlint:doc-detached", // A Scaladoc comment appears to be detached from its element.
  "-Xlint:inaccessible", // Warn about inaccessible types in method signatures.
  "-Xlint:missing-interpolator", // A string literal appears to be missing an interpolator id.
  "-Xlint:nullary-override", // Warn when non-nullary `def f()' overrides nullary `def f'.
  "-Xlint:nullary-unit", // Warn when nullary methods return Unit.
  "-Xlint:option-implicit", // Option.apply used implicit view.
  "-Xlint:package-object-classes", // Class or object defined in package object.
  "-Xlint:poly-implicit-overload", // Parameterized overloaded implicit methods are not visible as view bounds.
  "-Xlint:private-shadow", // A private field (or class parameter) shadows a superclass field.
  "-Xlint:stars-align", // Pattern sequence wildcard must align with sequence component.
  "-Xlint:type-parameter-shadow", // A local type parameter shadows a type already in scope.
  "-Xlint:unsound-match" // Pattern match may not be typesafe.
)

val Scala212Options = Seq(
  "-Xlint:infer-any", // Warn when a type argument is inferred to be `Any`.
  "-Ypartial-unification", // Enable partial unification in type constructor inference,
  "-Ywarn-extra-implicit", // Warn when more than one implicit parameter section is defined.
  "-Ywarn-unused:implicits", // Warn if an implicit parameter is unused.
  "-Ywarn-unused:imports", // Warn if an import selector is not referenced.
  "-Ywarn-unused:locals", // Warn if a local definition is unused.
  "-Ywarn-unused:params", // Warn if a value parameter is unused.
  "-Ywarn-unused:patvars", // Warn if a variable bound in a pattern is unused.
  "-Ywarn-unused:privates" // Warn if a private member is unused.
) ++ XLintOptions

val YWarnOptions = Seq(
  "-Ywarn-dead-code", // Warn when dead code is identified.
  "-Ywarn-inaccessible", // Warn about inaccessible types in method signatures.
  "-Ywarn-nullary-override", // Warn when non-nullary `def f()' overrides nullary `def f'.
  "-Ywarn-nullary-unit", // Warn when nullary methods return Unit.
  "-Ywarn-numeric-widen", // Warn when numerics are widened.
  "-Ywarn-value-discard" // Warn when non-Unit expression results are unused.
)

val scalacOptionsFn: String => Seq[String] = { s =>
  CrossVersion.partialVersion(s) match {
    case Some((_, minor)) if minor >= 12 => ScalacOptions ++ YWarnOptions ++ Scala212Options
    case _ => ScalacOptions ++ YWarnOptions
  }
}

scalacOptions in Global ++= scalacOptionsFn(scalaVersion.value)

lazy val Versions = new {
  val logback = "1.2.3"
  val util = "0.48.0"
  val json4s = "3.6.2"
  val datastax = "3.6.0"
  val scalatest = "3.0.5"
  val shapeless = "2.3.3"
  val thrift = "0.8.0"
  val finagle = "17.12.0"
  val scalameter = "0.8.2"
  val scalacheck = "1.14.0"
  val slf4j = "1.7.25"
  val reactivestreams = "1.0.2"
  val cassandraUnit = "3.5.0.1"
  val joda = "2.10.1"
  val jodaConvert = "2.1.2"
  val scalamock = "3.6.0"
  val macrocompat = "1.1.1"
  val macroParadise = "2.1.1"
  val circe = "0.9.2"

  val scala210 = "2.10.6"
  val scala211 = "2.11.12"
  val scala212 = "2.12.8"
  val monix = "2.3.3"
  val scalaAll = Seq(scala210, scala211, scala212)

  val scala = new {
    val all = Seq(scala210, scala211, scala212)
  }

  val typesafeConfig: String = if (Publishing.isJdk8) {
    "1.3.3"
  } else {
    "1.2.0"
  }

  val twitterUtil: String => String = {
    s => CrossVersion.partialVersion(s) match {
      case Some((_, minor)) if minor >= 12 => "6.45.0"
      case _ => "6.34.0"
    }
  }

  val akka: String => String = {
    s => CrossVersion.partialVersion(s) match {
      case Some((_, minor)) if minor >= 11 && Publishing.isJdk8 => "2.4.14"
      case _ => "2.3.15"
    }
  }

  val lift: String => String = {
    s => CrossVersion.partialVersion(s) match {
      case Some((_, minor)) if minor >= 11 => "3.0"
      case _ => "3.0-M1"
    }
  }

  val scrooge: String => String = {
    s => CrossVersion.partialVersion(s) match {
      case Some((_, minor)) if minor >= 11 && Publishing.isJdk8 => "4.18.0"
      case Some((_, minor)) if minor >= 11 && !Publishing.isJdk8 => "4.7.0"
      case _ => "4.7.0"
    }
  }
  val play: String => String = {
    s => CrossVersion.partialVersion(s) match {
      case Some((_, minor)) if minor == 12 => "2.6.1"
      case Some((_, minor)) if minor == 11 => "2.5.8"
      case _ => "2.4.8"
    }
  }


  val macrosVersion: String => String = {
    s => CrossVersion.partialVersion(s) match {
      case Some((_, minor)) if minor >= 11 => macroParadise
      case _ => "2.1.0"
    }
  }
}


val releaseSettings = Seq(
  releaseTutFolder in ThisBuild := baseDirectory.value / "docs",
  releaseIgnoreUntrackedFiles := true,
  releaseVersionBump := sbtrelease.Version.Bump.Minor,
  releaseTagComment := s"Releasing ${(version in ThisBuild).value} $ciSkipSequence",
  releaseCommitMessage := s"Setting version to ${(version in ThisBuild).value} $ciSkipSequence",
  releaseProcess := Seq[ReleaseStep](
    checkSnapshotDependencies,
    inquireVersions,
    setReleaseVersion,
    commitReleaseVersion,
    releaseStepTask((tut in Tut) in readme),
    commitTutFiles,
    releaseStepCommandAndRemaining("such publishSigned"),
    releaseStepCommandAndRemaining("sonatypeReleaseAll"),
    tagRelease,
    setNextVersion,
    commitNextVersion,
    pushChanges
  )
)

val defaultConcurrency = 4

val sharedSettings: Seq[Def.Setting[_]] = Defaults.coreDefaultSettings ++ Seq(
  organization := "com.outworkers",
  scalaVersion := Versions.scala212,
  credentials ++= Publishing.defaultCredentials,
  updateOptions := updateOptions.value.withCachedResolution(true),
  resolvers ++= Seq(
    "Twitter Repository" at "http://maven.twttr.com",
    Resolver.typesafeRepo("releases"),
    Resolver.sonatypeRepo("releases"),
    Resolver.jcenterRepo
  ),

  logLevel in Compile := { if (Publishing.runningUnderCi) Level.Error else Level.Info },
  logLevel in Test := Level.Info,
  libraryDependencies ++= Seq(
    "ch.qos.logback" % "logback-classic" % Versions.logback % Test,
    "org.slf4j" % "log4j-over-slf4j" % Versions.slf4j
  ),
  fork in Test := true,
  scalacOptions in (Compile, console) := ScalacOptions.filterNot(
    Set(
      "-Ywarn-unused:imports",
      "-Xfatal-warnings"
    )
  ),
  javaOptions in Test ++= Seq(
    "-Xmx2G",
    "-Djava.net.preferIPv4Stack=true",
    "-Dio.netty.resourceLeakDetection"
  ),
  envVars := Map("SCALACTIC_FILL_FILE_PATHNAMES" -> "yes"),
  parallelExecution in ThisBuild := false
) ++ Publishing.effectiveSettings

lazy val baseProjectList: Seq[ProjectReference] = Seq(
  phantomDsl,
  phantomExample,
  phantomConnectors,
  phantomFinagle,
  phantomStreams,
  phantomThrift,
  phantomSbtPlugin,
  phantomMonix,
  readme
)

lazy val fullProjectList = baseProjectList ++ Publishing.addOnCondition(Publishing.isJdk8, phantomJdk8)

lazy val phantom = (project in file("."))
  .settings(
    sharedSettings ++ Publishing.noPublishSettings
  ).settings(
    name := "phantom",
    moduleName := "phantom",
    commands += Command.command("testsWithCoverage") { state =>
      "coverage" ::
      "test" ::
      "coverageReport" ::
      "coverageAggregate" ::
      "coveralls" ::
      state
    }
  ).aggregate(
    fullProjectList: _*
  )

lazy val readme = (project in file("readme"))
  .settings(sharedSettings ++ Publishing.noPublishSettings)
  .settings(
    crossScalaVersions := Seq(Versions.scala211, Versions.scala212),
    tutSourceDirectory := sourceDirectory.value / "main" / "tut",
    tutTargetDirectory := phantom.base / "docs",
    libraryDependencies ++= Seq(
      "org.typelevel" %% "macro-compat" % Versions.macrocompat % "tut",
      "org.scala-lang" % "scala-compiler" % scalaVersion.value % "tut",
      compilerPlugin("org.scalamacros" % "paradise" % Versions.macrosVersion(scalaVersion.value) cross CrossVersion.full),
      "com.outworkers" %% "util-samplers" % Versions.util % "tut",
      "io.circe" %% "circe-parser" % Versions.circe % "tut",
      "io.circe" %% "circe-generic" % Versions.circe % "tut",
      "org.scalatest" %% "scalatest" % Versions.scalatest % "tut"
    )
  ).dependsOn(
    phantomDsl,
    phantomJdk8,
    phantomExample,
    phantomConnectors,
    phantomFinagle,
    phantomStreams,
    phantomThrift
  ).enablePlugins(TutPlugin, CrossPerProjectPlugin)

lazy val phantomDsl = (project in file("phantom-dsl"))
  .settings(sharedSettings: _*)
  .settings(
    name := "phantom-dsl",
    moduleName := "phantom-dsl",
    crossScalaVersions := Versions.scalaAll,
    concurrentRestrictions in Test := Seq(
      Tags.limit(Tags.ForkedTestGroup, defaultConcurrency)
    ),
    libraryDependencies ++= Seq(
      "org.typelevel" %% "macro-compat" % Versions.macrocompat,
      "org.scala-lang" % "scala-compiler" % scalaVersion.value % "provided",
      compilerPlugin("org.scalamacros" % "paradise" % Versions.macrosVersion(scalaVersion.value) cross CrossVersion.full),
      "com.chuusai"                  %% "shapeless"                         % Versions.shapeless,
      "joda-time"                    %  "joda-time"                         % Versions.joda,
      "org.joda"                     %  "joda-convert"                      % Versions.jodaConvert,
      "com.datastax.cassandra"       %  "cassandra-driver-core"             % Versions.datastax,
      "org.json4s"                   %% "json4s-native"                     % Versions.json4s % Test,
      "io.circe"                     %% "circe-parser"                      % Versions.circe % Test,
      "io.circe"                     %% "circe-generic"                     % Versions.circe % Test,
      "org.scalamock"                %% "scalamock-scalatest-support"       % Versions.scalamock % Test,
      "org.scalacheck"               %% "scalacheck"                        % Versions.scalacheck % Test,
      "com.outworkers"               %% "util-samplers"                     % Versions.util % Test,
      "com.storm-enroute"            %% "scalameter"                        % Versions.scalameter % Test,
      "ch.qos.logback"               % "logback-classic"                    % Versions.logback % Test
    )
  ).dependsOn(
    phantomConnectors
  ).enablePlugins(
    CrossPerProjectPlugin
  )

lazy val phantomJdk8 = (project in file("phantom-jdk8"))
  .settings(
    name := "phantom-jdk8",
    moduleName := "phantom-jdk8",
    crossScalaVersions := Versions.scalaAll,
    testOptions in Test += Tests.Argument("-oF"),
    concurrentRestrictions in Test := Seq(
      Tags.limit(Tags.ForkedTestGroup, defaultConcurrency)
    ),
    libraryDependencies ++= Seq(
      compilerPlugin("org.scalamacros" % "paradise" % Versions.macrosVersion(scalaVersion.value) cross CrossVersion.full)
    )
  ).settings(
    sharedSettings: _*
  ).dependsOn(
    phantomDsl % "compile->compile;test->test"
  ).enablePlugins(
    CrossPerProjectPlugin
  )

lazy val phantomConnectors = (project in file("phantom-connectors"))
  .settings(
    sharedSettings: _*
  ).settings(
    name := "phantom-connectors",
    moduleName := "phantom-connectors",
    crossScalaVersions := Versions.scalaAll,
    libraryDependencies ++= Seq(
      "com.datastax.cassandra"       %  "cassandra-driver-core"             % Versions.datastax,
      "com.outworkers"               %% "util-testing"                      % Versions.util % Test
    )
  ).enablePlugins(
    CrossPerProjectPlugin
  )

lazy val phantomFinagle = (project in file("phantom-finagle"))
  .settings(sharedSettings: _*)
  .settings(
    name := "phantom-finagle",
    moduleName := "phantom-finagle",
    crossScalaVersions := Versions.scalaAll,
    testFrameworks in Test ++= Seq(new TestFramework("org.scalameter.ScalaMeterFramework")),
    libraryDependencies ++= Seq(
      compilerPlugin("org.scalamacros" % "paradise" % Versions.macrosVersion(scalaVersion.value) cross CrossVersion.full),
      "com.twitter"                  %% "util-core"                         % Versions.twitterUtil(scalaVersion.value),
      "com.outworkers"               %% "util-testing"                      % Versions.util % Test,
      "com.storm-enroute"            %% "scalameter"                        % Versions.scalameter % Test
    )
  ).dependsOn(
    phantomDsl % "compile->compile;test->test"
  ).enablePlugins(
   CrossPerProjectPlugin
  )

lazy val phantomThrift = (project in file("phantom-thrift"))
  .settings(
    crossScalaVersions := Seq(Versions.scala211, Versions.scala212),
    name := "phantom-thrift",
    moduleName := "phantom-thrift",
    libraryDependencies ++= Seq(
      compilerPlugin("org.scalamacros" % "paradise" % Versions.macrosVersion(scalaVersion.value) cross CrossVersion.full),
      "org.typelevel" %% "macro-compat" % Versions.macrocompat,
      "org.scala-lang" % "scala-compiler" % scalaVersion.value % "provided",
      "org.apache.thrift"            % "libthrift"                          % Versions.thrift,
      "com.twitter"                  %% "scrooge-core"                      % Versions.scrooge(scalaVersion.value),
      "com.twitter"                  %% "scrooge-serializer"                % Versions.scrooge(scalaVersion.value),
      "com.outworkers"               %% "util-testing"                      % Versions.util % Test
    ),
    coverageExcludedPackages := "com.outworkers.phantom.thrift.models.*"
  ).settings(
    sharedSettings: _*
  ).dependsOn(
    phantomDsl % "compile->compile;test->test;",
    phantomFinagle
  ).enablePlugins(
    CrossPerProjectPlugin
  )

lazy val phantomSbtPlugin = (project in file("phantom-sbt"))
  .settings(
    sharedSettings: _*
  ).settings(
    name := "phantom-sbt",
    moduleName := "phantom-sbt",
    crossScalaVersions := Seq(Versions.scala210),
    publishMavenStyle := false,
    sbtPlugin := true,
    publishArtifact := scalaVersion.value.startsWith("2.10"),
    libraryDependencies ++= Seq(
      "com.datastax.cassandra" % "cassandra-driver-core" % Versions.datastax,
      "org.cassandraunit" % "cassandra-unit"  % Versions.cassandraUnit excludeAll (
        ExclusionRule("org.slf4j", "slf4j-log4j12"),
        ExclusionRule("org.slf4j", "slf4j-jdk14")
      )
    )
  ).enablePlugins(
    CrossPerProjectPlugin
  )

lazy val phantomStreams = (project in file("phantom-streams"))
  .settings(
    name := "phantom-streams",
    moduleName := "phantom-streams",
    crossScalaVersions := Versions.scalaAll,
    testFrameworks in Test ++= Seq(new TestFramework("org.scalameter.ScalaMeterFramework")),
    libraryDependencies ++= Seq(
      compilerPlugin("org.scalamacros" % "paradise" % Versions.macrosVersion(scalaVersion.value) cross CrossVersion.full),
      "com.typesafe" % "config" % Versions.typesafeConfig force(),
      "com.typesafe.play"   %% "play-iteratees" % Versions.play(scalaVersion.value) exclude ("com.typesafe", "config"),
      "org.reactivestreams" % "reactive-streams"            % Versions.reactivestreams,
      "com.typesafe.akka"   %% s"akka-actor"                % Versions.akka(scalaVersion.value) exclude ("com.typesafe", "config"),
      "com.outworkers"      %% "util-testing"               % Versions.util            % Test,
      "org.reactivestreams" % "reactive-streams-tck"        % Versions.reactivestreams % Test,
      "com.storm-enroute"   %% "scalameter"                 % Versions.scalameter      % Test
    )
  ).settings(
    sharedSettings: _*
  ).dependsOn(
    phantomDsl % "compile->compile;test->test"
  ).enablePlugins(
    CrossPerProjectPlugin
  )

lazy val phantomExample = (project in file("phantom-example"))
  .settings(
    name := "phantom-example",
    moduleName := "phantom-example",
    crossScalaVersions := Seq(Versions.scala211, Versions.scala212),
    libraryDependencies ++= Seq(
      compilerPlugin("org.scalamacros" % "paradise" % Versions.macrosVersion(scalaVersion.value) cross CrossVersion.full),
      "org.json4s"                   %% "json4s-native"                     % Versions.json4s % Test,
      "com.outworkers"               %% "util-samplers"                      % Versions.util % Test
    ),
    coverageExcludedPackages := "com.outworkers.phantom.example.basics.thrift.*"
  ).settings(
    sharedSettings: _*
  ).settings(
    Publishing.noPublishSettings
  ).dependsOn(
    phantomDsl % "test->test;compile->compile;",
    phantomThrift
  ).enablePlugins(
    CrossPerProjectPlugin
  )

  lazy val phantomMonix = (project in file("phantom-monix"))
    .settings(
      name := "phantom-monix",
      crossScalaVersions := Versions.scalaAll,
      moduleName := "phantom-monix",
      libraryDependencies ++= Seq(
        "com.outworkers" %% "util-testing" % Versions.util % Test,
        "org.scalatest" %% "scalatest" % Versions.scalatest % Test,
        compilerPlugin("org.scalamacros" % "paradise" % Versions.macrosVersion(scalaVersion.value) cross CrossVersion.full),
        "io.monix" %% "monix" % Versions.monix
      )
    ).settings(
    sharedSettings: _*
  ).dependsOn(
    phantomDsl % "compile->compile;test->test;"
  )