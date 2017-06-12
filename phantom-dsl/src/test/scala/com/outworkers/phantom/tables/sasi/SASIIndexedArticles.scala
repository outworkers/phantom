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
package com.outworkers.phantom.tables.sasi

import com.outworkers.phantom.builder.query.sasi.Analyzer.StandardAnalyzer
import com.outworkers.phantom.dsl._
import com.outworkers.phantom.tables.Article

abstract class SASIIndexedArticles extends Table[SASIIndexedArticles, Article] {
  object id extends UUIDColumn with PartitionKey
  object name extends StringColumn

  object orderId extends LongColumn with SASIIndex[StandardAnalyzer] {
    override def analyzer: StandardAnalyzer = Analyzer.StandardAnalyzer.enableStemming(true)
  }
}
