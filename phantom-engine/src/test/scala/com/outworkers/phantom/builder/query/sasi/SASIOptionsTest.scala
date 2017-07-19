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

import java.util.Locale

import com.outworkers.phantom.PhantomSuite

class SASIOptionsTest extends PhantomSuite {

  it should "automatically produce default options for a NonTokenizingAnalyzer" in {
    val query = Analyzer.NonTokenizingAnalyzer[Mode.Sparse]().qb.queryString
    query shouldEqual "OPTIONS = {'mode': 'SPARSE', 'analyzer_class': 'org.apache.cassandra.index.sasi.analyzer.NonTokenizingAnalyzer'}"
  }

  it should "allow setting case sensitivity to true on a NonTokenizingAnalyzer" in {
    val query = Analyzer.NonTokenizingAnalyzer[Mode.Sparse]().caseSensitive(true).qb.queryString

    query shouldEqual
      """OPTIONS = {'mode': 'SPARSE', 'analyzer_class': 'org.apache.cassandra.index.sasi.analyzer.NonTokenizingAnalyzer', 'case_sensitive': 'true'}"""
  }

  it should "allow setting case sensitivity to false on a NonTokenizingAnalyzer" in {
    val query = Analyzer.NonTokenizingAnalyzer[Mode.Sparse]().caseSensitive(false).qb.queryString

    query shouldEqual
      """OPTIONS = {'mode': 'SPARSE', 'analyzer_class': 'org.apache.cassandra.index.sasi.analyzer.NonTokenizingAnalyzer', 'case_sensitive': 'false'}"""
  }

  it should "allow setting normalise_lowercase to false on a NonTokenizingAnalyzer" in {
    val query = Analyzer.NonTokenizingAnalyzer[Mode.Sparse]().normalizeLowercase(false).qb.queryString

    query shouldEqual
      """OPTIONS = {'mode': 'SPARSE', 'analyzer_class': 'org.apache.cassandra.index.sasi.analyzer.NonTokenizingAnalyzer', 'normalize_lowercase': 'false'}"""
  }

  it should "allow setting normalise_uppercase to false on a NonTokenizingAnalyzer" in {
    val query = Analyzer.NonTokenizingAnalyzer[Mode.Sparse]().normalizeUppercase(false).qb.queryString

    query shouldEqual "OPTIONS = {'mode': 'SPARSE', 'analyzer_class': 'org.apache.cassandra.index.sasi.analyzer.NonTokenizingAnalyzer', 'normalize_uppercase': 'false'}"
  }

  it should "allow combining case sensitivity and normalisation on NonTokenizingAnalyzer" in {
    val query = Analyzer.NonTokenizingAnalyzer[Mode.Sparse]()
      .caseSensitive(true)
      .normalizeUppercase(false)
      .normalizeLowercase(true)
      .qb.queryString

    query shouldEqual "OPTIONS = {'mode': 'SPARSE', 'analyzer_class': 'org.apache.cassandra.index.sasi.analyzer.NonTokenizingAnalyzer', 'case_sensitive': 'true', " +
      "'normalize_uppercase': 'false', 'normalize_lowercase': 'true'}"
  }

  it should "automatically produce default options for a StandardAnalyzer" in {
    val query = Analyzer.StandardAnalyzer[Mode.Prefix]().qb.queryString
    query shouldEqual "OPTIONS = {'mode': 'PREFIX', 'analyzer_class': 'org.apache.cassandra.index.sasi.analyzer.StandardAnalyzer'}"
  }

  it should "allow using tokenization_normalise_lowecase on StandardAnalyzer" in {
    val query = Analyzer.StandardAnalyzer[Mode.Prefix]().normalizeLowercase(true).qb.queryString
    query shouldEqual "OPTIONS = {'mode': 'PREFIX', 'analyzer_class': 'org.apache.cassandra.index.sasi.analyzer.StandardAnalyzer', 'tokenization_normalize_lowercase': 'true'}"
  }

  it should "allow using tokenization_normalise_uppercase on StandardAnalyzer" in {
    val query = Analyzer.StandardAnalyzer[Mode.Prefix]().normalizeUppercase(true).qb.queryString
    query shouldEqual "OPTIONS = {'mode': 'PREFIX', 'analyzer_class': 'org.apache.cassandra.index.sasi.analyzer.StandardAnalyzer', 'tokenization_normalize_uppercase': 'true'}"
  }

  it should "allow using skip_stop_words on StandardAnalyzer" in {
    val query = Analyzer.StandardAnalyzer[Mode.Prefix]().skipStopWords(true).qb.queryString
    query shouldEqual "OPTIONS = {'mode': 'PREFIX', 'analyzer_class': 'org.apache.cassandra.index.sasi.analyzer.StandardAnalyzer', 'tokenization_skip_stop_words': 'true'}"
  }

  it should "allow using enable_stemming on StandardAnalyzer" in {
    val query = Analyzer.StandardAnalyzer[Mode.Prefix]().enableStemming(true).qb.queryString
    query shouldEqual "OPTIONS = {'mode': 'PREFIX', 'analyzer_class': 'org.apache.cassandra.index.sasi.analyzer.StandardAnalyzer', 'tokenization_enable_stemming': 'true'}"
  }

  it should "allow passing a string locale to StandardAnalyzer.locale" in {
    val query = Analyzer.StandardAnalyzer[Mode.Prefix]().locale("EN").qb.queryString
    query shouldEqual "OPTIONS = {'mode': 'PREFIX', 'analyzer_class': 'org.apache.cassandra.index.sasi.analyzer.StandardAnalyzer', 'tokenization_locale': 'EN'}"
  }

  it should "allow passing a Java Locale to StandardAnalyzer.locale" in {
    val query = Analyzer.StandardAnalyzer[Mode.Prefix]().locale(Locale.ENGLISH).qb.queryString
    query shouldEqual "OPTIONS = {'mode': 'PREFIX', 'analyzer_class': 'org.apache.cassandra.index.sasi.analyzer.StandardAnalyzer', 'tokenization_locale': 'English'}"
  }
}
