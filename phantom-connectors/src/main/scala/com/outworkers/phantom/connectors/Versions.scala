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
package com.outworkers.phantom.connectors

import com.datastax.driver.core.{VersionNumber => DatastaxVersionNumber}

sealed trait VersionBuilder {
  def apply(major: Int, minor: Int, patch: Int): DatastaxVersionNumber = {
    DatastaxVersionNumber.parse(s"$major.$minor.$patch")
  }
}

private object Version extends VersionBuilder

object DefaultVersions {
  val `2.0.8` = Version(2, 0, 8)
  val `2.0.9` = Version(2, 0, 9)
  val `2.0.10` = Version(2, 0, 10)
  val `2.0.11` = Version(2, 0, 11)
  val `2.0.12` = Version(2, 0, 12)
  val `2.0.13` = Version(2, 0, 13)
  val `2.1.0` = Version(2, 1, 0)
  val `2.1.1` = Version(2, 1, 1)
  val `2.1.2` = Version(2, 1, 2)
  val `2.1.3` = Version(2, 1, 3)
  val `2.1.4` = Version(2, 1, 4)
  val `2.1.5` = Version(2, 1, 5)
  val `2.1.6` = Version(2, 1, 6)
  val `2.1.7` = Version(2, 1, 7)
  val `2.1.8` = Version(2, 1, 8)
  val `2.1.9` = Version(2, 1, 9)
  val `2.2.0` = Version(2, 2, 0)
  val `2.2.1` = Version(2, 2, 1)
  val `2.2.2` = Version(2, 2, 2)
  val `2.2.8` = Version(2, 2, 8)
  val `2.3.0` = Version(2, 3, 0)
  val `3.0.0` = Version(3, 0, 0)
  val `3.1.0` = Version(3, 1, 0)
  val `3.2.0` = Version(3, 2, 0)
  val `3.3.0` = Version(3, 3, 0)
  val `3.4.0` = Version(3, 4, 0)
  val `3.5.0` = Version(3, 5, 0)
  val `3.6.0` = Version(3, 6, 0)
  val `3.7.0` = Version(3, 7, 0)
  val `3.8.0` = Version(3, 8, 0)
}