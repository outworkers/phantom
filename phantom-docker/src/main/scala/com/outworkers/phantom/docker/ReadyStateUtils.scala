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

import java.util.{Timer, TimerTask}

import sbt.Logger

import scala.concurrent.{ExecutionContext, Future, Promise}
import scala.concurrent.duration.FiniteDuration

class ReadyStateUtils(logger: Logger) {

  def withDelay[T](delay: Long)(f: => Future[T]): Future[T] = {
    val timer = new Timer()
    val promise = Promise.apply[T]

    timer.schedule(new TimerTask {
      override def run(): Unit = {
        promise.completeWith(f)
        timer.cancel()
      }
    }, delay)

    promise.future
  }

  /**
    * Loops through a future using a conditional check.
    * If the future fails or if the condition does not evaluate to true on the underlying result
    * of the future, then this will keep attempting to re-execute the future as many times as the attemps
    * parameter, interleaving a delay between each evaluation.
    * @param future The underyling future to re-evaluate.
    * @param condition The predicate defining the condition that should be true and mark the successful end
    *                  of attempts.
    * @param attempts The number of attempts to perform.
    * @param delay The delay in seconds in between each attempt.
    * @param ec The execution context in which to execute the request.
    * @return Returns a Future[T] only if the result was successful before we ran out of attempts, returns a failed
    *         future otherwise.
    */
  def looped(
    future: => Future[Boolean],
    condition: Boolean => Boolean,
    attempts: Int,
    delay: FiniteDuration
  )(
    implicit ec: ExecutionContext
  ): Future[Boolean] = {

    def attempt(rest: Int): Future[Boolean] = {
      future.flatMap { t =>
        if (condition(t)) {
          logger.info("Condition successfully evaluated to true")
          Future.successful(t)
        } else {
          rest match {
            case 0 =>
              logger.error("Ran out of attempts for retrying ready checker")
              Future.failed(new RuntimeException(s"Condition not true after $attempts attempts"))
            case n =>
              logger.info(s"Condition evaluated to false for $t, retrying again in $delay, ${n - 1} attempts left.")
              withDelay(delay.toMillis)(attempt(n - 1))
          }
        }
      } recoverWith {
        case e =>
          rest match {
            case 0 =>
              logger.error("Future failed and we have no attempts left")
              logger.trace(e)
              Future.failed(e match {
                case _: NoSuchElementException =>
                  new NoSuchElementException(
                    s"Ready checker returned false after $attempts attempts, delayed $delay each")
                case _ => e
              })
            case n =>
              logger.info(s"Future failed to return a value, retrying again in $delay, ${n - 1} attempts left.")
              logger.info(s"ERROR: ${e.getMessage}")
              logger.trace(e)
              withDelay(delay.toMillis)(attempt(n - 1))
          }
      }
    }

    attempt(attempts)
  }
}