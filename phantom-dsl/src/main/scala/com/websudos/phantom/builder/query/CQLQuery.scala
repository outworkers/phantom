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

import com.websudos.phantom.Manager
import com.websudos.phantom.builder.serializers.Utils

import com.websudos.diesel.engine.query.AbstractQuery

import org.json4s._

case class CQLQuery(override val queryString: String) extends AbstractQuery[CQLQuery](queryString) with Utils {
  def create(str: String): CQLQuery = CQLQuery(str)
}

object CQLQuery extends JsonUtils {
  def empty: CQLQuery = CQLQuery("")

  def escape(str: String): String = "'" + str.replaceAll("'", "''") + "'"

  def apply(collection: TraversableOnce[String]): CQLQuery = {
    val list = collection.map(x => {
      val bool = containsUppercaseChar(x)

      if (!bool) x else CQLQuery.empty.append(x).queryString
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

  def handleCaseSensitiveFieldNames[Record](json: String, formats: Formats): String = {
    Manager.logger.debug(s"handleCaseSensitiveFieldNames called for json ${json}")

    val jObject = parseToJObject(json)

    Manager.logger.debug(s"parsed JObject => ${jObject}")

    val modifiedJObject = recurseAndEscapeDoubleQuote(jObject)

    Manager.logger.debug(s"modified JObject => ${modifiedJObject}")

    writeToString(modifiedJObject.asInstanceOf[JObject], formats)
  }

  def escapeDoubleQuotes(s: String): String = {
    val ret = if (containsUppercaseChar(s) && !s.startsWith( """"""") && !s.startsWith( """\"""")) {
      """"""" + s + """""""
    } else s

    Manager.logger.debug(s"escapeDoubleQuotes called with string ${s} and return value ${ret}")

    ret
  }

  def escapeBackslashDoubleQuotes(s: String): String = {
    val ret = if (containsUppercaseChar(s) && !s.startsWith( """\"""")) {
      """\"""" + s + """\""""
    } else s

    Manager.logger.debug(s"escapeDoubleQuotes called with string ${s} and return value ${ret}")

    ret
  }

  def escapeDoubleQuotesQuery(s: String): CQLQuery = {
    val ret = if (containsUppercaseChar(s) && !s.startsWith( """"""")) {
      CQLQuery( """"""" + s + """"""")
    } else CQLQuery(s)

    Manager.logger.debug(s"escapeDoubleQuotesQuery called with string ${s} and return value ${ret}")

    ret
  }

  def escapeDoubleQuotesQuery(cql: CQLQuery): CQLQuery = {
    val s = cql.queryString
    val ret = if (containsUppercaseChar(s) && !s.startsWith( """"""")) {
      CQLQuery( """"""" + s + """"""")
    } else CQLQuery(s)

    Manager.logger.debug(s"escapeDoubleQuotesQuery called with string ${s} and return value ${ret}")

    ret
  }
}
