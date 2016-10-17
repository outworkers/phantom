package com.outworkers.phantom

import java.util.concurrent.Executors

import org.slf4j.LoggerFactory

import scala.concurrent.{ExecutionContext, ExecutionContextExecutor}

object Manager {

  private[this] lazy val taskExecutor = Executors.newCachedThreadPool()

  implicit lazy val scalaExecutor: ExecutionContextExecutor = ExecutionContext.fromExecutor(taskExecutor)

  lazy val logger = LoggerFactory.getLogger("com.websudos.phantom")

  /**
    * Shuts down the default task executors for Guava ListenableFutures and for Scala Futures.
    * @deprecated ("Call shutdown on a [[com.outworkers.phantom.database.Database]] instead", "1.15.0")
    */
  def shutdown(): Unit = {
    logger.info("Shutting down executors")
    taskExecutor.shutdown()
  }
}
