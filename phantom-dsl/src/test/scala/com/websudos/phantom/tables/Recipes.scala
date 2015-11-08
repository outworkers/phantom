/*
 * Copyright 2013-2015 Websudos, Limited.
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * - Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 *
 * - Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *
 * - Explicit consent must be obtained from the copyright owner, Websudos Limited before any redistribution is made.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package com.websudos.phantom.tables

import com.websudos.phantom.builder.query.InsertQuery
import com.websudos.phantom.dsl._
import com.websudos.phantom.testkit._
import org.joda.time.DateTime

case class Recipe(
  url: String,
  description: Option[String],
  ingredients: List[String],
  servings: Option[Int],
  lastCheckedAt: DateTime,
  props: Map[String, String],
  uid: UUID
)

class Recipes extends CassandraTable[Recipes, Recipe] {

  object url extends StringColumn(this) with PartitionKey[String]

  object description extends OptionalStringColumn(this)

  object ingredients extends ListColumn[Recipes, Recipe, String](this)

  object servings extends OptionalIntColumn(this)

  object lastcheckedat extends DateTimeColumn(this)

  object props extends MapColumn[Recipes, Recipe, String, String](this)

  object uid extends UUIDColumn(this)


  override def fromRow(r: Row): Recipe = {
    Recipe(
      url(r),
      description(r),
      ingredients(r),
      servings(r),
      lastcheckedat(r),
      props(r),
      uid(r)
    )
  }

}

object Recipes extends Recipes with PhantomCassandraConnector {

  override def tableName = "Recipes"

  def store(recipe: Recipe): InsertQuery.Default[Recipes, Recipe] = {
    insert
      .value(_.url, recipe.url)
      .value(_.description, recipe.description)
      .value(_.ingredients, recipe.ingredients)
      .value(_.lastcheckedat, recipe.lastCheckedAt)
      .value(_.props, recipe.props)
      .value(_.uid, recipe.uid)
      .value(_.servings, recipe.servings)
  }
}


case class SampleEvent(id: UUID, map: Map[Long, DateTime])

sealed class Events extends CassandraTable[Events, SampleEvent] with PhantomCassandraConnector {
  
  object id extends UUIDColumn(this) with PartitionKey[UUID]

  object map extends MapColumn[Events, SampleEvent, Long, DateTime](this)

  def fromRow(row: Row): SampleEvent = SampleEvent(
    id(row),
    map(row)
  )
}

object Events extends Events {

  def store(event: SampleEvent): InsertQuery.Default[Events, SampleEvent] = {
    insert.value(_.id, event.id)
      .value(_.map, event.map)
  }

  def getById(id: UUID) = {
    select.where(_.id eqs id)
  }
}