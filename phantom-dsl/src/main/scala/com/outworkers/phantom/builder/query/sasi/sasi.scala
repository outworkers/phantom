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

import com.outworkers.phantom.builder.query.OptionPart
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

sealed abstract class Analyzer[A <: Analyzer[A]](options: OptionPart) {
  protected[this] def instance(optionPart: OptionPart): A

  def mode(mode: Mode): A = instance(options option (CQLSyntax.SASI.mode, mode.value))

  def this(analyzerClass: AnalyzerClass, options: OptionPart) {
    this(options option (CQLSyntax.SASI.analyzer_class, analyzerClass.value))
  }
}


object Analyzer {

  class StandardAnalyzer(options: OptionPart) extends Analyzer[StandardAnalyzer](
    AnalyzerClass.StandardAnalyzer,
    OptionPart.empty
  ) {
    override protected[this] def instance(optionPart: OptionPart): StandardAnalyzer = {
      new StandardAnalyzer(optionPart)
    }

    def normalizeUppercase(flag: Boolean): StandardAnalyzer = {
      instance(options option(CQLSyntax.SASI.tokenization_normalize_uppercase, flag.toString))
    }

    def normalizeLowercase(flag: Boolean): StandardAnalyzer = {
      instance(options option(CQLSyntax.SASI.tokenization_normalize_lowercase, flag.toString))
    }

    def skipStopWords(flag: Boolean): StandardAnalyzer = {
      instance(options option(CQLSyntax.SASI.tokenization_skip_stop_words, flag.toString))
    }

    def enableStemming(flag: Boolean): StandardAnalyzer = {
      instance(options option(CQLSyntax.SASI.tokenization_enable_stemming, flag.toString))
    }

    def locale(locale: String): StandardAnalyzer = {
      instance(options option(CQLSyntax.SASI.tokenization_locale, locale))
    }

    def locale(locale: Locale): StandardAnalyzer = {
      instance(options option(CQLSyntax.SASI.tokenization_locale, locale.getDisplayName))
    }
  }

  class NonTokenizingAnalyzer(options: OptionPart) extends Analyzer[NonTokenizingAnalyzer](
    AnalyzerClass.NonTokenizingAnalyzer,
    OptionPart.empty
  ) {
    override protected[this] def instance(optionPart: OptionPart): NonTokenizingAnalyzer = {
      new NonTokenizingAnalyzer(optionPart)
    }

    def caseSensitive(flag: Boolean): NonTokenizingAnalyzer = {
      instance(options option (CQLSyntax.SASI.case_sensitive, flag.toString))
    }

    def normalizeUppercase(flag: Boolean): NonTokenizingAnalyzer = {
      instance(options option(CQLSyntax.SASI.normalize_uppercase, flag.toString))
    }

    def normalizeLowercase(flag: Boolean): NonTokenizingAnalyzer = {
      instance(options option(CQLSyntax.SASI.normalize_lowercase, flag.toString))
    }
  }
}


