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
package com.outworkers.phantom.docker

import java.nio.charset.StandardCharsets
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.{AtomicBoolean, AtomicReference}

import com.spotify.docker.client.DockerClient.AttachParameter
import com.spotify.docker.client.{DefaultDockerClient, DockerClient}
import com.whisk.docker.impl.spotify.SpotifyDockerFactory
import com.whisk.docker.{DockerCommandExecutor, DockerContainer, DockerContainerState, DockerReadyChecker}
import sbt.Keys._
import sbt._

import scala.collection.JavaConverters._
import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}

object SbtPlugin extends AutoPlugin {

  override def requires: Plugins = sbt.plugins.JvmPlugin

  private[this] final val internalCqlPort: Int = 9042
  final val defaultCqlPort: Int = 9142

  final val defaultPullImagesTimeout: Duration = 20.minutes
  final val defaultStartContainersTimeout: Duration = 1.minute
  final val defaultStopContainersTimeout: Duration = 1.minute
  final val defaultCasandraStartupAwaitTime: Duration = 2.minutes

  val state = new AtomicBoolean(false)
  val dockerKitRef = new AtomicReference[Option[DockerSbtKit]](None)

  val defaultCassandraContainer = DockerContainer("cassandra:3.11")

  val client = DefaultDockerClient.fromEnv().build()

  def withLogStreamLinesRequirement(
    logger: Logger,
    client: DockerClient,
    id: String,
    withErr: Boolean
  )(f: (String) => Boolean)(implicit ec: ExecutionContext): Future[Boolean] = {

    val baseParams = List(AttachParameter.STDOUT, AttachParameter.STREAM, AttachParameter.LOGS)
    val logParams = if (withErr) AttachParameter.STDERR :: baseParams else baseParams

    val streamF = Future(client.attachContainer(id, logParams: _*))

    streamF.map { stream =>
      stream.asScala.find(t => {
        val log = StandardCharsets.US_ASCII.decode(t.content()).toString
        logger.info(s"$log [checked]")
        f(log)
      }) match {
        case Some(t) => {
          val log = StandardCharsets.US_ASCII.decode(t.content()).toString
          logger.info("++++++++++++++++++++ Found the message we were looking for!!!!!!!!!!!")
          logger.info(log)
          true
        }
        case None => false
      }
    }
  }

  def initCassandraContainer(
    logger: Logger,
    cassandraPort: Int,
    cassandraDockerImage: DockerContainer,
    cassandraStartupAwaitTime: Duration,
    pullTimeout: Duration,
    startTimeout: Duration,
    stopTimeout: Duration
  ): Unit = {
    logger.info(s"Using Docker image ${cassandraDockerImage.image} to launch Cassandra")

    val readyStateUtils = new ReadyStateUtils(logger)

    case class LogLineContains(str: String) extends DockerReadyChecker {
      override def apply(container: DockerContainerState
      )(
        implicit docker: DockerCommandExecutor,
        ec: ExecutionContext
      ): Future[Boolean] = {
        for {
          id <- container.id
          check <- withLogStreamLinesRequirement(logger, client, id, withErr = true) { s =>
            s.contains(str)
          }
        } yield check
      }
    }

    case class Looped(
      underlying: DockerReadyChecker,
      attempts: Int,
      delay: FiniteDuration
    ) extends DockerReadyChecker {

      override def apply(container: DockerContainerState)(
        implicit docker: DockerCommandExecutor,
        ec: ExecutionContext
      ): Future[Boolean] = {
        readyStateUtils.looped(
          underlying(container),
          identity,
          attempts,
          delay
        )
      }
    }
    val lineCheck = "Starting listening for CQL clients"

    val cassandraContainer = cassandraDockerImage
      .withPorts(internalCqlPort -> Some(cassandraPort))
      .withReadyChecker(
        Looped(
          LogLineContains(lineCheck),
          60,
          1.seconds
        )
      )

    val dockerKit = new DockerSbtKit(
      new SpotifyDockerFactory(client),
      logger,
      cassandraContainer :: Nil,
      pullTimeout,
      startTimeout,
      stopTimeout
    )

    dockerKitRef.set(Some(dockerKit))

    logger.info("Starting docker container")

    dockerKit.startAllOrFail()
    logger.info("Successfully started Cassandra Docker container")
  }

  def shuwdownCassandraDockerContainer(logger: Logger): Unit = {
    val ref = dockerKitRef.get()
    if (ref.isEmpty) {
      throw new IllegalStateException("There appears to be no DockerKit created within this runtime, this shouldn't happen")
    } else {
      ref.get.stopAllQuietly(() => {
        state.compareAndSet(true, false)
      })
      logger.info("Successfully stopped all Docker containers")
    }
  }

  val cassandraPort = settingKey[Int]("The default CQL port to use. This will be exposed from within Docker.")
  val cassandraDockerImage = settingKey[String]("Cassandra image to use as source for the docker repository")
  val cassandraDockerImageName = settingKey[Option[String]]("Image name to use for the docker container")
  val dockerStartTimeoutSeconds = settingKey[Long]("Seconds to wait for Docker containers to startup up")
  val dockerStopTimeoutSeconds = settingKey[Long]("Seconds to wait for Docker containers to shut down")
  val dockerPullTimeoutMinutes = settingKey[Int]("Minutes to wait for docker images to be resolved")
  val cassandraStartAwaitTime = settingKey[Long]("Milliseconds to wait or Cassandra ")

  val startCassandraDockerContainer = taskKey[Unit]("Starts up a Cassandra cluster in Docker")
  val startCassandra = taskKey[Unit]("Starts up a Docker container and waits for Cassandra to start inside it")
  val stopCassandraContainer = taskKey[Unit]("Stop the Cassandra Docker container")

  val cassandraSettings: Seq[Def.Setting[_]] = Seq(
    logBuffered := true,
    cassandraPort := defaultCqlPort,
    cassandraDockerImage := "cassandra:3.11",
    cassandraDockerImageName := None,

    cassandraStartAwaitTime := { defaultCasandraStartupAwaitTime.toMillis },
    dockerPullTimeoutMinutes := { defaultPullImagesTimeout.toMinutes.toInt },
    dockerStartTimeoutSeconds := { defaultStartContainersTimeout.toSeconds },
    dockerStopTimeoutSeconds := { defaultStopContainersTimeout.toSeconds },
    stopCassandraContainer := { shuwdownCassandraDockerContainer(streams.value.log) },
    startCassandraDockerContainer := {
      val logger = streams.value.log

      val realPort = cassandraPort.value

      logger.info(s"Using port $realPort for Cassandra CQL interface. Automatically exposing this from Docker")

      val name = cassandraDockerImageName.value
      logger.info(s"Using $name for the Docker Cassandra container")

      val cassandraBaseImage = DockerContainer(cassandraDockerImage.value, name)
      logger.info(s"Using Docker image ${cassandraBaseImage.image} to launch Cassandra")

      val pullTimeout = Duration(dockerPullTimeoutMinutes.value, TimeUnit.MINUTES)
      logger.info(s"Waiting for $pullTimeout for Docker dependencies to resolve")

      val startTimeout = Duration(dockerStartTimeoutSeconds.value, TimeUnit.SECONDS)
      logger.info(s"Waiting for $startTimeout for docker containers to start")

      val stopTimeout = Duration(dockerStopTimeoutSeconds.value, TimeUnit.SECONDS)
      //logger.info(s"Will wait for $stopTimeout for docker contains to stop")

      val cassandraAwaitStartupTimeout = cassandraStartAwaitTime.value.millis
      //logger.info(s"Will sleep for $cassandraAwaitStartupTimeout milliseconds to wait for Cassandra to start")

      if (state.compareAndSet(false, true)) {
        logger.info("Cassandra container not running, attempting to start Docker container")

        initCassandraContainer(
          streams.value.log,
          cassandraPort = realPort,
          cassandraDockerImage = cassandraBaseImage,
          cassandraStartupAwaitTime = cassandraAwaitStartupTimeout,
          pullTimeout = pullTimeout,
          startTimeout = startTimeout,
          stopTimeout = stopTimeout
        )
      } else {
        logger.info("Cassandra container is already running, not attempting to start it again")
      }
    },
    fork := true,
    testOptions in Test += Tests.Cleanup(() => dockerKitRef.get().foreach(_.stopAllQuietly())),
    commands += Command.command("testWithCassandra") { state =>
      "startCassandraDockerContainer" ::
        "test" ::
        state
    }
  )
}