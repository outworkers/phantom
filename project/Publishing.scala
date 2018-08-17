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
import sbt.Keys._
import sbt._
import com.typesafe.sbt.pgp.PgpKeys._
import sbtrelease.ReleasePlugin.autoImport.{ReleaseStep, _}
import sbtrelease.ReleaseStateTransformations._
import sbtrelease.Vcs

import scala.util.Properties

object Publishing {

  lazy val noPublishSettings = Seq(
    publish := (),
    publishLocal := (),
    publishArtifact := false
  )

  val ciSkipSequence = "[ci skip]"

  private def toProcessLogger(st: State): ProcessLogger = new ProcessLogger {
    override def error(s: => String): Unit = st.log.error(s)
    override def info(s: => String): Unit = st.log.info(s)
    override def buffer[T](f: => T): T = st.log.buffer(f)
  }

  def vcs(state: State): Vcs = {
    Project.extract(state).get(releaseVcs)
      .getOrElse(sys.error("Aborting release. Working directory is not a repository of a recognized VCS."))
  }

  val releaseTutFolder = settingKey[File]("The file to write the version to")

  def commitTutFilesAndVersion: ReleaseStep = ReleaseStep { st: State =>
    val settings = Project.extract(st)
    println(s"Found modified files: ${vcs(st).hasModifiedFiles}")

    val log = toProcessLogger(st)
    val versionsFile = settings.get(releaseVersionFile).getCanonicalFile
    val docsFolder = settings.get(releaseTutFolder).getCanonicalFile

    println(s"Docs folder path : ${docsFolder.getPath}")
    println(s"Docs folder path : ${docsFolder.getAbsolutePath}")
    val base = vcs(st).baseDir.getCanonicalFile
    val sign = settings.get(releaseVcsSign)

    val relativePath = IO.relativize(
      base,
      versionsFile
    ).getOrElse("Version file [%s] is outside of this VCS repository with base directory [%s]!" format(versionsFile, base))

    val commitablePaths = Seq(relativePath) ++ {
      if (docsFolder.exists) {
        println(s"Docs folder exists under ${docsFolder}")
        val relativeDocsPath = IO.relativize(
          base,
          docsFolder
        ).getOrElse("Docs folder [%s] is outside of this VCS repository with base directory [%s]!" format(docsFolder, base))
        Seq(relativeDocsPath)
      } else {
        println(s"Docs folder doesn't exist under, $base and $docsFolder")
        Seq.empty
      }
    }

    vcs(st).add(commitablePaths: _*) !! log
    val status = (vcs(st).status !!) trim

    val newState = if (status.nonEmpty) {
      val (state, msg) = settings.runTask(releaseCommitMessage, st)
      vcs(state).commit(msg, sign) !! log
      state
    } else {
      // nothing to commit. this happens if the version.sbt file hasn't changed or no docs have been added.
      st
    }
    vcs(newState).status !! log

    newState
  }

  val releaseSettings = Seq(
    releaseTutFolder in ThisBuild := baseDirectory.value / "docs",
    releaseIgnoreUntrackedFiles := true,
    releaseVersionBump := sbtrelease.Version.Bump.Bugfix,
    releaseTagComment := s"Releasing ${(version in ThisBuild).value} $ciSkipSequence",
    releaseCommitMessage := s"Setting version to ${(version in ThisBuild).value} $ciSkipSequence",
    releaseProcess := Seq[ReleaseStep](
      checkSnapshotDependencies,
      inquireVersions,
      setReleaseVersion,
      commitReleaseVersion,
      publishArtifacts,
      tagRelease,
      releaseStepCommandAndRemaining("such publish"),
      setNextVersion,
      commitTutFilesAndVersion,
      pushChanges
    )
  )

  lazy val defaultCredentials: Seq[Credentials] = {
    if (!Publishing.runningUnderCi) {
      Seq(
        Credentials(Path.userHome / ".bintray" / ".credentials"),
        Credentials(Path.userHome / ".ivy2" / ".credentials")
      )
    } else {
      Seq(
        Credentials(
          realm = "Bintray",
          host = "dl.bintray.com",
          userName = System.getenv("bintray_user"),
          passwd = System.getenv("bintray_password")
        ),
        Credentials(
          realm = "Sonatype OSS Repository Manager",
          host = "oss.sonatype.org",
          userName = System.getenv("maven_user"),
          passwd = System.getenv("maven_password")
        ),
        Credentials(
          realm = "Bintray API Realm",
          host = "api.bintray.com",
          userName = System.getenv("bintray_user"),
          passwd = System.getenv("bintray_password")
        )
      )
    }
  }

  def publishToMaven: Boolean = sys.env.get("MAVEN_PUBLISH").exists("true" ==)

  lazy val bintraySettings: Seq[Def.Setting[_]] = {
    import bintray.BintrayKeys._
    Seq(
      publishMavenStyle := true,
      bintrayOrganization := Some("outworkers"),
      bintrayRepository := { if (version.value.trim.endsWith("SNAPSHOT")) "oss-snapshots" else "oss-releases" },
      bintrayReleaseOnPublish in ThisBuild := true,
      publishArtifact in Test := false,
      pomIncludeRepository := { _ => true},
      licenses += ("Apache-2.0", url("https://github.com/outworkers/phantom/blob/develop/LICENSE.txt"))
    )
  }

  def effectiveSettings: Seq[Def.Setting[_]] = {
    if (publishToMaven) {
      bintraySettings ++ releaseSettings
    } else {
      // Intentional silly way to disable Bintray publishing temporarily
      //bintraySettings ++ releaseSettings
      bintraySettings ++ releaseSettings
    }
  }

  def runningUnderCi: Boolean = sys.env.get("CI").isDefined || sys.env.get("TRAVIS").isDefined
  def travisScala211: Boolean = sys.env.get("TRAVIS_SCALA_VERSION").exists(_.contains("2.11"))

  def isTravisScala210: Boolean = !travisScala211

  def isJdk8: Boolean = sys.props("java.specification.version") == "1.8"

  lazy val addOnCondition: (Boolean, ProjectReference) => Seq[ProjectReference] = (bool, ref) =>
    if (bool) ref :: Nil else Nil

  lazy val addRef: (Boolean, ClasspathDep[ProjectReference]) => Seq[ClasspathDep[ProjectReference]] = (bool, ref) =>
    if (bool) Seq(ref) else Seq.empty

}
