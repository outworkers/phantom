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

import com.outworkers.phantom.builder.primitives.Primitive
import com.outworkers.phantom.builder.syntax.CQLSyntax

trait SASIWrapperType {
  def value: String
}

trait PrefixValue extends SASIWrapperType

object PrefixValue {

  def apply(v: String): PrefixValue = new PrefixValue {
    override def value: String = v
  }

  implicit def prefixEv(implicit ev: Primitive[String]): Primitive[PrefixValue] = {
    Primitive.derive[PrefixValue, String](
      t => t.value + CQLSyntax.Symbols.percent
    )(PrefixValue.apply)
  }
}

trait SuffixValue extends SASIWrapperType

object SuffixValue {

  def apply(v: String): SuffixValue = new SuffixValue {
    override def value: String = v
  }

  implicit def suffixEv(implicit ev: Primitive[String]): Primitive[SuffixValue] = {
    Primitive.derive[SuffixValue, String](
      t => CQLSyntax.Symbols.percent + t.value
    )(SuffixValue.apply)
  }
}

trait ContainsValue extends SASIWrapperType

object ContainsValue {

  def apply(v: String): ContainsValue = new ContainsValue {
    override def value: String = v
  }

  implicit def containsEv(implicit ev: Primitive[String]): Primitive[ContainsValue] = {
    Primitive.derive[ContainsValue, String](
      t => CQLSyntax.Symbols.percent + t.value + CQLSyntax.Symbols.percent
    )(ContainsValue.apply)
  }
}