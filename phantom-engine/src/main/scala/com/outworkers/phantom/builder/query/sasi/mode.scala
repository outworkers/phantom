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

import com.outworkers.phantom.builder.syntax.CQLSyntax

abstract class Mode

object Mode {
  class Contains extends Mode
  class Prefix extends Mode
  class Sparse extends Mode
}

trait ModeDef[M <: Mode] {
  def value: String
}

object ModeDef {
  implicit val containsDef: ModeDef[Mode.Contains] = new ModeDef[Mode.Contains] {
    override def value: String = CQLSyntax.SASI.Modes.Contains
  }

  implicit val sparseDef: ModeDef[Mode.Sparse] = new ModeDef[Mode.Sparse] {
    override def value: String = CQLSyntax.SASI.Modes.Sparse
  }

  implicit val prefixDef: ModeDef[Mode.Prefix] = new ModeDef[Mode.Prefix] {
    override def value: String = CQLSyntax.SASI.Modes.Prefix
  }
}