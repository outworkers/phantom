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
 * - Explicit consent must be obtained from the copyright owner, Outworkers Limited before any redistribution is made.
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
import com.typesafe.sbt.pgp.PgpKeys._
import sbt._
import sbt.Keys._
import bintray.BintrayKeys._

object PublishTasks {

  val bintrayPublishSettings: Seq[Def.Setting[_]] = Seq(
    publishMavenStyle := true,
    bintrayOrganization := Some("websudos"),
    bintrayRepository <<= scalaVersion.apply {
      v => if (v.trim.endsWith("SNAPSHOT")) "oss-snapshots" else "oss-releases"
    },
    bintrayReleaseOnPublish in ThisBuild := true,
    publishArtifact in Test := false,
    pomIncludeRepository := { _ => true},
    licenses += ("Apache-2.0", url("https://github.com/outworkers/phantom/blob/develop/LICENSE.txt"))
  )

  val mavenPublishingSettings: Seq[Def.Setting[_]] = Seq(
    credentials += Credentials(Path.userHome / ".ivy2" / ".credentials"),
    publishMavenStyle := true,
    publishTo <<= version.apply {
      v =>
        val nexus = "https://oss.sonatype.org/"
        if (v.trim.endsWith("SNAPSHOT")) {
          Some("snapshots" at nexus + "content/repositories/snapshots")
        } else {
          Some("releases" at nexus + "service/local/staging/deploy/maven2")
        }
    },
    externalResolvers <<= resolvers map { rs =>
      Resolver.withDefaultResolvers(rs, mavenCentral = true)
    },
    licenses += ("Outworkers License", url("https://github.com/outworkers/phantom/blob/develop/LICENSE.txt")),
    publishArtifact in Test := false,
    pomIncludeRepository := { _ => true },
    pomExtra :=
      <url>https://github.com/outworkers/phantom</url>
        <scm>
          <url>git@github.com:outworkers/phantom.git</url>
          <connection>scm:git:git@github.com:outworkers/phantom.git</connection>
        </scm>
        <developers>
          <developer>
            <id>alexflav</id>
            <name>Flavian Alexandru</name>
            <url>http://github.com/alexflav23</url>
          </developer>
        </developers>
  )

  lazy val mavenPublishSettings : TaskKey[Unit] = TaskKey[Unit]("mavenPublishSettings")

  lazy val mavenPublishConfiguration = TaskKey[PublishConfiguration](
    "mavenPublishConfiguration",
    "Changed the configuration to a Sonatype publishing one"
  )

  lazy val publishToMavenCentral = taskKey[Unit]("publishToMavenCentral")

  val printedPublishing = settingKey[Option[String]]("printedPublishing")

  val mavenTaskSettings: Seq[Def.Setting[_]] = Seq(
    mavenPublishSettings := mavenPublishingSettings,
    mavenPublishConfiguration <<= (
      mavenPublishSettings,
      signedArtifacts,
      publishTo,
      deliver,
      checksums in publish,
      ivyLoggingLevel
      ) map { (_, arts, publishTo, ivyFile, checks, level) => {}
      Classpaths.publishConfig(
        arts,
        None,
        resolverName = Classpaths.getPublishTo(publishTo).name,
        checksums = checks,
        logging = level,
        overwrite = false
      )
    },
    publishToMavenCentral <<= Classpaths.publishTask(mavenPublishConfiguration, deliver)
      .dependsOn(mavenPublishConfiguration)
  )
}