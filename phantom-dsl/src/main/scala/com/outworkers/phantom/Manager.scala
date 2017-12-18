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
package com.outworkers.phantom

import java.util.concurrent.Executors
import org.slf4j.{Logger, LoggerFactory}
import scala.concurrent.{ExecutionContext, ExecutionContextExecutor}

object Manager {

  private[this] lazy val taskExecutor = Executors.newCachedThreadPool()

  implicit lazy val scalaExecutor: ExecutionContextExecutor = ExecutionContext.fromExecutor(taskExecutor)

  val logger: Logger = LoggerFactory.getLogger("com.outworkers.phantom")

  /**
    * Shuts down the default task executors for Guava ListenableFutures and for Scala Futures.
    * @deprecated ("Call shutdown on a [[com.outworkers.phantom.database.Database]] instead", "1.15.0")
    */
  def shutdown(): Unit = {
    logger.info("Shutting down executors")
    taskExecutor.shutdown()
  }
}
