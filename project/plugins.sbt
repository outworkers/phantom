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
resolvers ++= Seq(
  "jgit-repo" at "http://download.eclipse.org/jgit/maven",
  "Twitter Repo" at "http://maven.twttr.com/",
  Resolver.sonatypeRepo("releases"),
  Resolver.bintrayIvyRepo("sksamuel", "sbt-plugins"),
  Resolver.bintrayIvyRepo("twittercsl-ivy", "sbt-plugins"),
  Resolver.bintrayRepo("twittercsl", "sbt-plugins")
)

lazy val scalaTravisEnv = sys.env.get("TRAVIS_SCALA_VERSION")
def isScala210: Boolean = scalaTravisEnv.exists("2.10.6" ==)
lazy val isCi = sys.env.get("CI").exists("true" == )

lazy val Versions = new {
  val scrooge = if (isCi) {
    if (sys.props("java.specification.version") == "1.8" && !isScala210) "19.3.0" else "19.3.0"
  } else {
    if (sys.props("java.specification.version") == "1.8") "19.3.0" else "19.3.0"
  }
}

addSbtPlugin("org.scoverage" %% "sbt-scoverage" % "1.5.1")

addSbtPlugin("org.scoverage" %% "sbt-coveralls" % "1.2.7")

// addSbtPlugin("io.get-coursier" % "sbt-coursier" % "1.1.0-M13-1")

addSbtPlugin("com.jsuereth" % "sbt-pgp" % "1.1.1")

addSbtPlugin("org.xerial.sbt" % "sbt-sonatype" % "2.5")

addSbtPlugin("com.typesafe.sbt" % "sbt-git" % "1.0.0")

addSbtPlugin("com.twitter" % "scrooge-sbt-plugin" % Versions.scrooge)

dependencyOverrides += "org.apache.thrift" % "libthrift" % "0.10.0"

addSbtPlugin("org.tpolecat" % "tut-plugin" % "0.6.10")

// addSbtPlugin("com.eed3si9n" % "sbt-doge" % "0.1.5")

libraryDependencies += "org.slf4j" % "slf4j-nop" % "1.7.22"

addSbtPlugin("com.github.gseitz" % "sbt-release" % "1.0.11")
