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
package com.outworkers.phantom.builder.query.sasi

import com.outworkers.phantom.PhantomSuite

class SASIOptionsTest extends PhantomSuite {

  it should "automatically produce default options for a NonTokenizingAnalyzer" in {
    val query = Analyzer.NonTokenizingAnalyzer.qb.queryString
    query shouldEqual "OPTIONS = {'analyzer_class': 'org.apache.cassandra.index.sasi.analyzer.NonTokenizingAnalyzer'}"
  }

  it should "allow setting case sensitivity to true on a NonTokenizingAnalyzer" in {
    val query = Analyzer.NonTokenizingAnalyzer.caseSensitive(true).qb.queryString

    query shouldEqual
      """
        |OPTIONS = {
        |'analyzer_class': 'org.apache.cassandra.index.sasi.analyzer.NonTokenizingAnalyzer',
        |'case_sensitive': 'true'
        |}
      """.stripMargin.replaceAll("\n", "")
  }


  it should "automatically produce default options for a StandardAnalyzer" in {
    val query = Analyzer.StandardAnalyzer.qb.queryString
    query shouldEqual "OPTIONS = {'analyzer_class': 'org.apache.cassandra.index.sasi.analyzer.StandardAnalyzer'}"
  }
}
