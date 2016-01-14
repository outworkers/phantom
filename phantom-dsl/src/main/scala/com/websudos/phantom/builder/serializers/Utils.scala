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
package com.websudos.phantom.builder.serializers

import com.websudos.phantom.builder.QueryBuilder.Utils
import com.websudos.phantom.builder.query.CQLQuery
import com.websudos.phantom.builder.syntax.CQLSyntax

private[builder] trait Utils {

  def concat(qb: CQLQuery, clause: CQLQuery): CQLQuery = {
    qb.forcePad.append(clause)
  }

  def concat(qb: CQLQuery, op: String, clause: CQLQuery): CQLQuery = {
    qb.pad.append(op).forcePad.append(clause)
  }

  def operator(op: String, clause: CQLQuery): CQLQuery = {
    CQLQuery(op).forcePad.append(clause)
  }

  def concat(column: String, op: String, value: String): CQLQuery = {
    CQLQuery(column).pad.append(op).forcePad.append(value)
  }

  def join(list: TraversableOnce[String]): CQLQuery = {
    CQLQuery(CQLSyntax.Symbols.`(`).append(list.mkString(", ")).append(CQLSyntax.Symbols.`)`)
  }

  def join(qbs: CQLQuery*): CQLQuery = {
    CQLQuery(qbs.map(_.queryString).mkString(", "))
  }

  def collection(list: TraversableOnce[String]): CQLQuery = {
    CQLQuery(CQLSyntax.Symbols.`[`).append(list.mkString(", ")).append(CQLSyntax.Symbols.`]`)
  }

  def set(list: Set[String]): CQLQuery = {
    CQLQuery(CQLSyntax.Symbols.`{`).append(list.mkString(", ")).append(CQLSyntax.Symbols.`}`)
  }

  def map(list: TraversableOnce[(String, String)]): CQLQuery = {
    CQLQuery(CQLSyntax.Symbols.`{`)
      .append(list.map(item => {s"${item._1} : ${item._2}"}).mkString(", "))
      .append(CQLSyntax.Symbols.`}`)
  }
  
  /**
    * Serializes the CQL definition of a map key based on a column and a key value.
    * When this method is called, the key should be already serialized using the Primitive API.
    * It will take 2 strings and produce an output of the following type:
    *
    * {{{
    *   QueryBuilder.Utils.mapKey("col", "test") == "col['test']"
    * }}}
    *
    * @param column The name of the column.
    * @param key The value of the key, pre-escaped and converted from a CQL Primitive to a string serialization.
    * @return
    */
  def mapKey(column: String, key: String): CQLQuery = {
    CQLQuery(column).append(CQLSyntax.Symbols.`[`)
      .append(key).append(CQLSyntax.Symbols.`]`)
  }

  def tableOption(option: String, value: String): CQLQuery = {
    Utils.concat(option, CQLSyntax.Symbols.`=`, value)
  }

  def tableOption(option: String, value: CQLQuery): CQLQuery = {
    tableOption(option, value.queryString)
  }
}

private[builder] object qUtils extends Utils
