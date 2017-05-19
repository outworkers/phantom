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
package com.outworkers.phantom.macros

import scala.collection.mutable.{ Map => MutableMap }
import scala.reflect.macros.blackbox

private[phantom] object BlackboxToolbelt {
  def apply(c: blackbox.Context): BlackboxToolbelt = new BlackboxToolbelt(c)

  final class Cache {
    val underlying: MutableMap[Any, Any] = MutableMap.empty
  }

  final val primitiveCache: Cache = new Cache()

  final val databaseHelperCache: Cache = new Cache()

  final val bindHelperCache: Cache = new Cache()
}

private[phantom] class BlackboxToolbelt(val c: blackbox.Context) {

  import c.universe._

  lazy val showAborts =
    !c.inferImplicitValue(typeOf[debug.optionTypes.ShowAborts], silent = true).isEmpty

  lazy val showCache =
    !c.inferImplicitValue(typeOf[debug.optionTypes.ShowCache], silent = true).isEmpty

  lazy val showTrees =
    !c.inferImplicitValue(typeOf[debug.optionTypes.ShowTrees], silent = true).isEmpty

  def memoize[A, B](cache: BlackboxToolbelt.Cache)(
    a: A, f: A => B
  ): B = cache.underlying.synchronized {
    cache.underlying.get(a) match {
      case Some(b: B @unchecked) =>
        if (showCache) c.echo(c.enclosingPosition, s"ShowCache: $b cached result $b")
        b
      case _ =>
        val b = f(a)
        cache.underlying += (a -> b)
        if (showCache) c.echo(c.enclosingPosition, s"ShowCache: $a computed result $b")
        b
    }
  }
}