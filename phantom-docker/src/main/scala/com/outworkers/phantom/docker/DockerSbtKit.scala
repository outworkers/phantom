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

import java.util.concurrent.Executors

import com.whisk.docker.{DockerContainer, DockerContainerManager, DockerContainerState, DockerFactory}
import sbt.Logger

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext, Future}

class DockerSbtKit(
  factory: DockerFactory,
  logger: Logger,
  dockerContainers: List[DockerContainer],
  pullImagesTimeout: Duration,
  startContainerTimeout: Duration,
  stopContainer: Duration
) {

  implicit def dockerFactory: DockerFactory = factory

  // we need ExecutionContext in order to run docker.init() / docker.stop() there
  implicit lazy val dockerExecutionContext: ExecutionContext = {
    // using Math.max to prevent unexpected zero length of docker containers
    ExecutionContext.fromExecutor(
      Executors.newFixedThreadPool(Math.max(1, dockerContainers.length * 2))
    )
  }
  implicit lazy val dockerExecutor = dockerFactory.createExecutor()

  lazy val containerManager = new DockerContainerManager(dockerContainers, dockerExecutor)

  def isContainerReady(container: DockerContainer): Future[Boolean] =
    containerManager.isReady(container)

  def getContainerState(container: DockerContainer): DockerContainerState = {
    containerManager.getContainerState(container)
  }

  implicit def containerToState(c: DockerContainer): DockerContainerState = {
    getContainerState(c)
  }


  def timed[T](f: => T): T = {
    val start = System.nanoTime()
    val x = f
    val end = System.nanoTime()
    val time = (end - start).nanos
    logger.info(s"Callback completed in ${time.toMillis} milliseconds")
    x
  }

  def waitForReadyChecks(duration: Duration): Unit = {
    logger.info("Waiting for all ready states to be complete")

    val futures = dockerContainers.map(container => container.readyChecker(getContainerState(container)))

    timed {
      Await.result(Future.sequence(futures), duration)
    }
  }

  def startAllOrFail(): Unit = {
    Await.result(containerManager.pullImages(), pullImagesTimeout)

    val allRunning: Boolean = try {
      val future: Future[Boolean] =
        containerManager.initReadyAll(startContainerTimeout).map(x => {
          logger.info("Finished initialising containers using containerManager.")
          logger.info(s"Container states: ${x.mkString(", ")}")
          x.map(_._2).forall(identity)
        })

      sys.addShutdownHook(
        containerManager.stopRmAll()
      )

      Await.result(future, startContainerTimeout)

    } catch {
      case e: Exception =>
        logger.error("Exception during container initialization")
        logger.trace(e)
        false
    }
    if (!allRunning) {
      Await.result(containerManager.stopRmAll(), stopContainer)
      throw new RuntimeException("Cannot run all required containers")
    }
  }

  def stopAllQuietly[T](f: => T): Unit = {
    try {
      Await.result(containerManager.stopRmAll() map ( _ => { val x = f }), stopContainer)
    } catch {
      case e: Throwable => {
        logger.error(e.getMessage)
        logger.trace(e)
      }
    }
  }


}