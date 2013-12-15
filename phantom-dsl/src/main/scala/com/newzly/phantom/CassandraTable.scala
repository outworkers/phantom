/*
 * Copyright 2013 newzly ltd.
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
package com.newzly.phantom

import java.io.Serializable
import scala.reflect.ClassTag

import com.datastax.driver.core.Row
import com.datastax.driver.core.querybuilder._

import com.newzly.phantom.query.{ CreateQuery, DeleteQuery, InsertQuery, SelectQuery, UpdateQuery}


abstract class CassandraTable[T <: CassandraTable[T, R], R] {

  def _key: Column[_]

  private[this] lazy val _name: String = {
    getClass.getName.split("\\.").toList.last.replaceAll("[^$]*\\$\\$[^$]*\\$[^$]*\\$|\\$\\$[^\\$]*\\$", "").dropRight(1)
  }

  def tableName: String = _name

  def fromRow(r: Row): R

  def select: SelectQuery[T, R] =
    new SelectQuery[T, R](this.asInstanceOf[T], QueryBuilder.select().from(tableName), this.asInstanceOf[T].fromRow)

  def select[A](f1: T => SelectColumn[A]): SelectQuery[T, A] = {
    val t = this.asInstanceOf[T]
    val c = f1(t)
    new SelectQuery[T, A](t, QueryBuilder.select(c.col.name).from(tableName), c.apply)
  }

  def select[A, B](f1: T => SelectColumn[A], f2: T => SelectColumn[B]): SelectQuery[T, (A, B)] = {
    val t = this.asInstanceOf[T]
    val c1 = f1(t)
    val c2 = f2(t)
    new SelectQuery[T, (A, B)](t, QueryBuilder.select(c1.col.name, c2.col.name).from(tableName), r => (c1(r), c2(r)))
  }

  def select[A, B, C](f1: T =>SelectColumn[A], f2: T => SelectColumn[B], f3: T => SelectColumn[C]): SelectQuery[T, (A, B, C)] = {
    val t = this.asInstanceOf[T]
    val c1 = f1(t)
    val c2 = f2(t)
    val c3 = f3(t)
    new SelectQuery[T, (A, B, C)](t, QueryBuilder.select(c1.col.name, c2.col.name, c3.col.name).from(tableName), r => (c1(r), c2(r), c3(r)))
  }

  def select[A, B, C, D](f1: T =>SelectColumn[A], f2: T => SelectColumn[B], f3: T => SelectColumn[C], f4: T => SelectColumn[D]): SelectQuery[T, (A, B, C, D)] = {
    val t = this.asInstanceOf[T]
    val c1 = f1(t)
    val c2 = f2(t)
    val c3 = f3(t)
    val c4 = f4(t)
    new SelectQuery[T, (A, B, C, D)](t, QueryBuilder.select(c1.col.name, c2.col.name, c3.col.name, c4.col.name).from(tableName), r => (c1(r), c2(r), c3(r), c4(r)))
  }

  def update = new UpdateQuery[T, R](this.asInstanceOf[T], QueryBuilder.update(tableName))

  def insert = new InsertQuery[T, R](this.asInstanceOf[T], QueryBuilder.insertInto(tableName))

  def delete = new DeleteQuery[T, R](this.asInstanceOf[T], QueryBuilder.delete.from(tableName))

  def create = new CreateQuery[T, R](this.asInstanceOf[T], "")

}
