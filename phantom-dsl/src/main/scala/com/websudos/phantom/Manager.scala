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
 * - Explicit consent must be obtained from the copyright owner, Websudos Limited before any redistribution is made.
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
package com.websudos.phantom

import java.util.concurrent.Executors

import com.google.common.util.concurrent.MoreExecutors
import com.websudos.phantom.builder.query.{CQLQuery, CreateImplicits, ExecutableStatementList}
import com.websudos.phantom.connectors.KeySpace
import org.slf4j.LoggerFactory

import scala.collection.mutable.{ArrayBuffer => MutableArrayBuffer}
import scala.concurrent.ExecutionContext

sealed class AutoCreate extends CreateImplicits {

  protected[this] lazy val _tables: MutableArrayBuffer[CassandraTable[_, _]] = new MutableArrayBuffer[CassandraTable[_, _]]
  private[phantom] lazy val tableList = _tables.toList

  def autoinit()(implicit keySpace: KeySpace): ExecutableStatementList = {
    new ExecutableStatementList(creations())
  }

  private[this] def creations()(implicit keySpace: KeySpace): List[CQLQuery] = {
    tableList map {
      table => table.create.ifNotExists().qb
    }
  }

  private[this] def truncations()(implicit keySpace: KeySpace): List[CQLQuery] = {
    tableList map {
      table => table.truncate().qb
    }
  }

  def autocreate()(implicit keySpace: KeySpace): ExecutableStatementList = {
    new ExecutableStatementList(creations())
  }

  def autotruncate()(implicit keySpace: KeySpace): ExecutableStatementList = {
    new ExecutableStatementList(truncations())
  }

  private[phantom] def addTable(table: CassandraTable[_, _]): Unit = {
    _tables += table
  }
}

object Manager extends AutoCreate {

  lazy val cores = Runtime.getRuntime.availableProcessors()

  lazy val taskExecutor = Executors.newCachedThreadPool()

  implicit lazy val scalaExecutor: ExecutionContext = ExecutionContext.fromExecutor(taskExecutor)

  lazy val executor = MoreExecutors.listeningDecorator(taskExecutor)

  lazy val logger = LoggerFactory.getLogger("com.websudos.phantom")

  def shutdown(): Unit = {
    logger.info("Shutting down executors")
    taskExecutor.shutdown()
    executor.shutdown()
  }
}