/*
 * Copyright 2013-2017 Outworkers, Limited.
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