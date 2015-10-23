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
package com.websudos.phantom.connectors

import com.datastax.driver.core.{ VersionNumber => DatastaxVersionNumber }

sealed trait VersionBuilder {
  def apply(major: Int, minor: Int, patch: Int): DatastaxVersionNumber = {
    DatastaxVersionNumber.parse(s"$major.$minor.$patch")
  }
}

object DefaultVersions extends VersionBuilder {
  val `2.0.8` = apply(2, 0, 8)
  val `2.0.9` = apply(2, 0, 9)
  val `2.0.10` = apply(2, 0, 10)
  val `2.0.11` = apply(2, 0, 11)
  val `2.0.12` = apply(2, 0, 12)
  val `2.0.13` = apply(2, 0, 13)
  val `2.1.0` = apply(2, 1, 0)
  val `2.1.1` = apply(2, 1, 1)
  val `2.1.2` = apply(2, 1, 2)
  val `2.1.3` = apply(2, 1, 3)
  val `2.1.4` = apply(2, 1, 4)
  val `2.1.5` = apply(2, 1, 5)
}