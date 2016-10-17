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
package com.outworkers.phantom.sbt

import java.util.concurrent.atomic.AtomicBoolean

import org.cassandraunit.utils.EmbeddedCassandraServerHelper
import sbt.Keys._
import sbt._

import scala.concurrent.blocking
import scala.util.control.NonFatal
import scala.util.{Failure, Success, Try}

/**
  * sbt plugin for starting Cassandra in embedded mode before running
  * the tests.
  *
  * First the plugin must be included in your `plugins.sbt`:
  *
  * {{{
  *   addSbtPlugin("com.websudos" %% "phantom-sbt" % phantomVersion)
  * }}}
  * The plugin does the following
  * things:
  *
  * - Automatically starts Cassandra in embedded mode whenever the test task is run
  * - Forces the tests for the projects that include the settings to always run in a
  *   forked JVM as this is the only way to make parallel tests using phantom work.
  *   (This is not caused by the implementation of this plugin or the new connector
  *   or zookeeper artifacts, this is caused by implementation details in the official
  *   `phantom-dsl` artifact, mainly the use of reflection which is not thread-safe
  *   in Scala 2.10)
  *
  * If you want to specify a custom Cassandra configuration,
  * you can do that with a setting:
  *
  * {{{
  * phantomCassandraConfig := baseDirectory.value / "config" / "cassandra.yaml"
  * }}}
  */
object PhantomSbtPlugin extends AutoPlugin {

  override def requires: Plugins = sbt.plugins.JvmPlugin

  /**
    * Keys for all settings of this plugin.
    */
  object autoImport {

    val phantomStartEmbeddedCassandra = taskKey[Unit]("Starts embedded Cassandra")
    val phantomCleanupEmbeddedCassandra = taskKey[Unit]("Clean up embedded Cassandra by dropping all of its keyspaces")
    val phantomCassandraConfig = settingKey[Option[File]]("YAML file for Cassandra configuration")
    val phantomCassandraTimeout = settingKey[Option[Int]]("Timeout in milliseconds for embedded Cassandra to start")
  }

  import autoImport._

  override def projectSettings: Seq[Def.Setting[_]] = Seq(
    phantomCassandraConfig := None,
    phantomCleanupEmbeddedCassandra := EmbeddedCassandra.cleanup(streams.value.log),
    test in Test <<= (test in Test).dependsOn(phantomStartEmbeddedCassandra),
    testQuick in Test <<= (testQuick in Test).dependsOn(phantomStartEmbeddedCassandra),
    testOnly in Test <<= (testOnly in Test).dependsOn(phantomStartEmbeddedCassandra),
    phantomCassandraTimeout := None,
    phantomStartEmbeddedCassandra := EmbeddedCassandra.start(
      streams.value.log,
      phantomCassandraConfig.value,
      phantomCassandraTimeout.value
    ),
    fork := true
  )
}

/**
  * Singleton object that is responsible for starting
  * Cassandra in embedded mode, but only once.
  * Subsequent calls to `start` will be ignored.
  */
object EmbeddedCassandra {

  private[this] val started: AtomicBoolean = new AtomicBoolean(false)

  /**
    * Starts Cassandra in embedded mode if it has not been
    * started yet.
    */
  def start(logger: Logger, config: Option[File] = None, timeout: Option[Int] = None): Unit = {
    this.synchronized {
      if (started.compareAndSet(false, true)) {
        blocking {
          val configFile = config.map(_.toURI.toString) getOrElse EmbeddedCassandraServerHelper.DEFAULT_CASSANDRA_YML_FILE
          System.setProperty("cassandra.config", configFile)
          Try {
            EmbeddedCassandraServerHelper.mkdirs()
          } match {
            case Success(value) => logger.info("Successfully created directories for embedded Cassandra.")
            case Failure(NonFatal(e)) =>
              logger.error(s"Error creating Embedded cassandra directories: ${e.getMessage}")
          }

          (config, timeout) match {
            case (Some(file), None) =>
              logger.info(s"Starting Cassandra in embedded mode with configuration from $file.")
              EmbeddedCassandraServerHelper.startEmbeddedCassandra(
                file,
                EmbeddedCassandraServerHelper.DEFAULT_TMP_DIR,
                EmbeddedCassandraServerHelper.DEFAULT_STARTUP_TIMEOUT
              )
            case (Some(file), Some(time)) =>
              logger.info(s"Starting Cassandra in embedded mode with configuration from $file and timeout set to $timeout ms.")
              EmbeddedCassandraServerHelper.startEmbeddedCassandra(
                file,
                EmbeddedCassandraServerHelper.DEFAULT_TMP_DIR,
                time
              )

            case (None, Some(time)) =>
              logger.info(s"Starting Cassandra in embedded mode with default configuration and timeout set to $timeout ms.")
              EmbeddedCassandraServerHelper.startEmbeddedCassandra(time)
            case (None, None) =>
              logger.info("Starting Cassandra in embedded mode with default configuration.")
              EmbeddedCassandraServerHelper.startEmbeddedCassandra()
          }
        }
      }
      else {
        logger.info("Embedded Cassandra has already been started")
      }
    }
  }


  def cleanup(logger: Logger): Unit = {
    this.synchronized {
      if (started.get()) {
        logger.info("Cleaning up embedded Cassandra")
        EmbeddedCassandraServerHelper.cleanEmbeddedCassandra()
      } else {
        logger.info("Cassandra is not running, not cleaning up")
      }
    }
  }
}
