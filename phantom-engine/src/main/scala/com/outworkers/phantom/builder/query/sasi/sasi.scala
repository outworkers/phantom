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

sealed abstract class AnalyzerClass(val value: String)

object AnalyzerClass {
  case object StandardAnalyzer extends AnalyzerClass(CQLSyntax.SASI.Analyzer.standard)
  case object NonTokenizingAnalyzer extends AnalyzerClass(CQLSyntax.SASI.Analyzer.nonTokenizing)
}

private[phantom] abstract class Analyzer[
  M <: Mode : ModeDef
](options: OptionPart) {
  protected[this] def instance(optionPart: OptionPart): Analyzer[M]

  def analyzed(flag: Boolean): Analyzer[M] = instance(options option (CQLSyntax.SASI.analyzed, flag.toString))

  def this(analyzerClass: AnalyzerClass, options: OptionPart) {
    this(
      OptionPart.empty
        .option(CQLSyntax.SASI.mode, CQLQuery.escape(implicitly[ModeDef[M]].value))
        .option(CQLSyntax.SASI.analyzer_class, CQLQuery.escape(analyzerClass.value))
        .append(options)
    )
  }

  def qb: CQLQuery = Utils.tableOption(CQLSyntax.SASI.options, options.qb)
}

class DefaultAnalyzer[M <: Mode : ModeDef](options: OptionPart) extends Analyzer[M](options) {
  override protected[this] def instance(optionPart: OptionPart): DefaultAnalyzer[M] = new DefaultAnalyzer(optionPart)
}

object Analyzer {

  def apply[M <: Mode : ModeDef](
    options: OptionPart = OptionPart.empty
  ): DefaultAnalyzer[M] = new DefaultAnalyzer(options)

  class StandardAnalyzer[M <: Mode : ModeDef](options: OptionPart) extends Analyzer[M](
    AnalyzerClass.StandardAnalyzer,
    options
  ) {
    override protected[this] def instance(optionPart: OptionPart): StandardAnalyzer[M] = {
      new StandardAnalyzer(optionPart)
    }

    def normalizeUppercase(flag: Boolean): StandardAnalyzer[M] = {
      instance(options option(CQLSyntax.SASI.tokenization_normalize_uppercase, flag))
    }

    def normalizeLowercase(flag: Boolean): StandardAnalyzer[M] = {
      instance(options option(CQLSyntax.SASI.tokenization_normalize_lowercase, flag))
    }

    def skipStopWords(flag: Boolean): StandardAnalyzer[M] = {
      instance(options option(CQLSyntax.SASI.tokenization_skip_stop_words, flag))
    }

    def enableStemming(flag: Boolean): StandardAnalyzer[M] = {
      instance(options option(CQLSyntax.SASI.tokenization_enable_stemming, flag))
    }

    def locale(loc: String): StandardAnalyzer[M] = {
      instance(options option(CQLSyntax.SASI.tokenization_locale, CQLQuery.escape(loc)))
    }

    def locale(loc: Locale): StandardAnalyzer[M] = {
      locale(loc.getDisplayName)
    }
  }

  object StandardAnalyzer {
    def apply[M <: Mode : ModeDef](opts: OptionPart = OptionPart.empty): StandardAnalyzer[M] = {
      new StandardAnalyzer[M](opts)
    }
  }

  sealed class NonTokenizingAnalyzer[M <: Mode : ModeDef](
    options: OptionPart
  ) extends Analyzer[M](
    AnalyzerClass.NonTokenizingAnalyzer,
    options
  ) {
    override protected[this] def instance(optionPart: OptionPart): NonTokenizingAnalyzer[M] = {
      new NonTokenizingAnalyzer(optionPart)
    }

    def caseSensitive(flag: Boolean): NonTokenizingAnalyzer[M] = {
      instance(options option (CQLSyntax.SASI.case_sensitive, flag))
    }

    def normalizeUppercase(flag: Boolean): NonTokenizingAnalyzer[M] = {
      instance(options option(CQLSyntax.SASI.normalize_uppercase, flag))
    }

    def normalizeLowercase(flag: Boolean): NonTokenizingAnalyzer[M] = {
      instance(options option(CQLSyntax.SASI.normalize_lowercase, flag))
    }
  }

  object NonTokenizingAnalyzer {
    def apply[M <: Mode : ModeDef](opts: OptionPart = OptionPart.empty): NonTokenizingAnalyzer[M] = {
      new NonTokenizingAnalyzer(opts)
    }
  }
}
