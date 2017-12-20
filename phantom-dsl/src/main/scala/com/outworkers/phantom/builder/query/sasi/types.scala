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

import com.outworkers.phantom.builder.QueryBuilder
import com.outworkers.phantom.builder.primitives.Primitive

trait SASIWrapperType {
  def value: String
}

trait PrefixValue extends SASIWrapperType

object PrefixValue {

  def apply(v: String): PrefixValue = new PrefixValue {
    override def value: String = v
  }

  implicit def prefixEv(implicit ev: Primitive[String]): Primitive[PrefixValue] = {
    Primitive.derive[PrefixValue, String](_.value.dropRight(1)) { str =>
      new PrefixValue {
        override def value: String = QueryBuilder.SASI.prefixValue(str).queryString
      }
    }
  }
}

trait SuffixValue extends SASIWrapperType

object SuffixValue {

  def apply(v: String): SuffixValue = new SuffixValue {
    override def value: String = v
  }

  implicit def suffixEv(implicit ev: Primitive[String]): Primitive[SuffixValue] = {
    Primitive.derive[SuffixValue, String](_.value.drop(1)) { str =>
      new SuffixValue {
        override def value: String = QueryBuilder.SASI.suffixValue(str).queryString
      }
    }
  }
}

trait ContainsValue extends SASIWrapperType

object ContainsValue {

  def apply(v: String): ContainsValue = new ContainsValue {
    override def value: String = v
  }

  implicit def containsEv(implicit ev: Primitive[String]): Primitive[ContainsValue] = {
    Primitive.derive[ContainsValue, String](source => source.value.slice(1, source.value.length - 1)) { str =>
      new ContainsValue {
        override def value: String = QueryBuilder.SASI.containsValue(str).queryString
      }
    }
  }
}