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
resolvers ++= Seq(
  "Sonatype snapshots" at "http://oss.sonatype.org/content/repositories/snapshots/",
  "sonatype-releases" at "https://oss.sonatype.org/content/repositories/releases/",
  "jgit-repo" at "http://download.eclipse.org/jgit/maven",
  "Twitter Repo" at "http://maven.twttr.com/",
  Resolver.bintrayRepo("websudos", "oss-releases"),
  Resolver.url("scoverage-bintray", url("https://dl.bintray.com/sksamuel/sbt-plugins/"))(Resolver.ivyStylePatterns),
  Resolver.url("bintray-csl-sbt-plugins", url("https://dl.bintray.com/twittercsl/sbt-plugins"))(Resolver.mavenStylePatterns),
  Resolver.url("twitter-csl-sbt-plugins", url("https://dl.bintray.com/twittercsl/sbt-plugins"))(Resolver.ivyStylePatterns)
)

addSbtPlugin("net.virtual-void" % "sbt-dependency-graph" % "0.7.5")

addSbtPlugin("org.scoverage" %% "sbt-scoverage" % "1.3.3")

addSbtPlugin("org.scoverage" %% "sbt-coveralls" % "1.0.3")

addSbtPlugin("com.jsuereth" % "sbt-pgp" % "1.0.0")

//addSbtPlugin("org.xerial.sbt" % "sbt-sonatype" % "1.1")

addSbtPlugin("org.scalastyle" %% "scalastyle-sbt-plugin" % "0.7.0")

addSbtPlugin("com.typesafe.sbt" % "sbt-git" % "0.8.5")

addSbtPlugin("me.lessis" % "bintray-sbt" % "0.3.0")

addSbtPlugin("com.websudos" % "sbt-package-dist" % "1.2.0")

addSbtPlugin("com.earldouglas" % "xsbt-web-plugin" % "2.0.4")

//addSbtPlugin("org.xerial.sbt" % "sbt-sonatype" % "1.1")

addSbtPlugin("com.twitter" % "scrooge-sbt-plugin" % "4.7.0")