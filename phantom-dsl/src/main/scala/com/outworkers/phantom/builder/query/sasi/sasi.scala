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

import com.outworkers.phantom.builder.QueryBuilder.Utils
import com.outworkers.phantom.builder.query.OptionPart
import com.outworkers.phantom.builder.query.engine.CQLQuery
import com.outworkers.phantom.builder.syntax.CQLSyntax

sealed abstract class Mode(val value: String)

object Mode {
  case object Contains extends Mode("CONTAINS")
  case object Prefix extends Mode("PREFIX")
  case object Sparse extends Mode("SPARSE")
}

sealed abstract class AnalyzerClass(val value: String)

object AnalyzerClass {
  case object StandardAnalyzer extends AnalyzerClass(CQLSyntax.SASI.Analyzer.standard)
  case object NonTokenizingAnalyzer extends AnalyzerClass(CQLSyntax.SASI.Analyzer.nonTokenizing)
}

private[phantom] abstract class Analyzer[A <: Analyzer[A]](options: OptionPart) {
  protected[this] def instance(optionPart: OptionPart): A

  def mode(mode: Mode): A = instance(options option (CQLSyntax.SASI.mode, mode.value))

  def analyzed(flag: Boolean): A = instance(options option (CQLSyntax.SASI.analyzed, flag.toString))

  def this(analyzerClass: AnalyzerClass, options: OptionPart) {
    this(OptionPart.empty.option(CQLSyntax.SASI.analyzer_class, CQLQuery.escape(analyzerClass.value)) append options)
  }

  def qb: CQLQuery = Utils.tableOption(CQLSyntax.SASI.options, options.qb)
}

class DefaultAnalyzer(options: OptionPart) extends Analyzer[DefaultAnalyzer](options) {
  override protected[this] def instance(optionPart: OptionPart): DefaultAnalyzer = new DefaultAnalyzer(optionPart)
}

object Analyzer {

  def apply(options: OptionPart = OptionPart.empty): DefaultAnalyzer = new DefaultAnalyzer(options)

  class StandardAnalyzer(options: OptionPart) extends Analyzer[StandardAnalyzer](
    AnalyzerClass.StandardAnalyzer,
    options
  ) {
    override protected[this] def instance(optionPart: OptionPart): StandardAnalyzer = {
      new StandardAnalyzer(optionPart)
    }

    def normalizeUppercase(flag: Boolean): StandardAnalyzer = {
      instance(options option(CQLSyntax.SASI.tokenization_normalize_uppercase, flag))
    }

    def normalizeLowercase(flag: Boolean): StandardAnalyzer = {
      instance(options option(CQLSyntax.SASI.tokenization_normalize_lowercase, flag))
    }

    def skipStopWords(flag: Boolean): StandardAnalyzer = {
      instance(options option(CQLSyntax.SASI.tokenization_skip_stop_words, flag))
    }

    def enableStemming(flag: Boolean): StandardAnalyzer = {
      instance(options option(CQLSyntax.SASI.tokenization_enable_stemming, flag))
    }

    def locale(loc: String): StandardAnalyzer = {
      instance(options option(CQLSyntax.SASI.tokenization_locale, CQLQuery.escape(loc)))
    }

    def locale(loc: Locale): StandardAnalyzer = {
      locale(loc.getDisplayName)
    }
  }

  object StandardAnalyzer extends StandardAnalyzer(OptionPart.empty)

  sealed class NonTokenizingAnalyzer(options: OptionPart) extends Analyzer[NonTokenizingAnalyzer](
    AnalyzerClass.NonTokenizingAnalyzer,
    options
  ) {
    override protected[this] def instance(optionPart: OptionPart): NonTokenizingAnalyzer = {
      new NonTokenizingAnalyzer(optionPart)
    }

    def caseSensitive(flag: Boolean): NonTokenizingAnalyzer = {
      instance(options option (CQLSyntax.SASI.case_sensitive, flag))
    }

    def normalizeUppercase(flag: Boolean): NonTokenizingAnalyzer = {
      instance(options option(CQLSyntax.SASI.normalize_uppercase, flag))
    }

    def normalizeLowercase(flag: Boolean): NonTokenizingAnalyzer = {
      instance(options option(CQLSyntax.SASI.normalize_lowercase, flag))
    }
  }

  object NonTokenizingAnalyzer extends NonTokenizingAnalyzer(OptionPart.empty)
}


