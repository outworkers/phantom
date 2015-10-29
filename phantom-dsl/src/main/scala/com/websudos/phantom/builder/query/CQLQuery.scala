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
package com.websudos.phantom.builder.query

import com.websudos.phantom.builder.serializers.Utils

import com.websudos.diesel.engine.query.AbstractQuery

case class CQLQuery(override val queryString: String) extends AbstractQuery[CQLQuery](queryString) with Utils {
  def create(str: String): CQLQuery = CQLQuery(str)
}

object CQLQuery {
  def empty: CQLQuery = CQLQuery("")

  def escape(str: String): String = "'" + str.replaceAll("'", "''") + "'"

  def apply(collection: TraversableOnce[String]): CQLQuery = {
    val list = collection.map(x => {
      val bool = containsUppercaseChar(x)

      if (!bool) x else CQLQuery.empty.appendIfAbsent("\"").append(x).appendIfAbsent("\"").queryString
    })
    CQLQuery(list.mkString(", "))
  }

  def containsUppercaseChar(qs: String): Boolean = {
    val l = for {
      c <- qs.toCharArray()
      if Character.isUpperCase(c)
    } yield true

    l.contains(true)
  }

}
