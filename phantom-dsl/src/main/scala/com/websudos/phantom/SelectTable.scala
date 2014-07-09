/*
 * Copyright 2013 websudos ltd.
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
package com.websudos.phantom

import com.datastax.driver.core.querybuilder.QueryBuilder
import com.websudos.phantom.column.SelectColumn
import com.websudos.phantom.query.SelectQuery

trait SelectTable[T <: CassandraTable[T, R], R] {
  self: CassandraTable[T, R] =>

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

  def select[A, B, C, D](
    f1: T =>SelectColumn[A],
   f2: T => SelectColumn[B],
   f3: T => SelectColumn[C],
   f4: T => SelectColumn[D]): SelectQuery[T, (A, B, C, D)] = {
    val t = this.asInstanceOf[T]
    val c1 = f1(t)
    val c2 = f2(t)
    val c3 = f3(t)
    val c4 = f4(t)
    new SelectQuery[T, (A, B, C, D)](t,
      QueryBuilder.select(c1.col.name, c2.col.name, c3.col.name, c4.col.name).from(tableName),
      r => (c1(r), c2(r), c3(r), c4(r))
    )
  }

  def select[A, B, C, D, E](f1: T =>SelectColumn[A], f2: T => SelectColumn[B], f3: T => SelectColumn[C], f4: T => SelectColumn[D], f5: T => SelectColumn[E]): SelectQuery[T, (A, B, C, D, E)] = {
    val t = this.asInstanceOf[T]
    val c1 = f1(t)
    val c2 = f2(t)
    val c3 = f3(t)
    val c4 = f4(t)
    val c5 = f5(t)
    new SelectQuery[T, (A, B, C, D, E)](t, QueryBuilder.select(c1.col.name, c2.col.name, c3.col.name, c4.col.name, c5.col.name).from(tableName), r => (c1(r), c2(r), c3(r), c4(r), c5(r)))
  }

  def select[A, B, C, D, E, F](
    f1: T =>SelectColumn[A],
    f2: T => SelectColumn[B],
    f3: T => SelectColumn[C],
    f4: T => SelectColumn[D],
    f5: T => SelectColumn[E],
    f6: T => SelectColumn[F]): SelectQuery[T, (A, B, C, D, E, F)] = {
    val t = this.asInstanceOf[T]
    val c1 = f1(t)
    val c2 = f2(t)
    val c3 = f3(t)
    val c4 = f4(t)
    val c5 = f5(t)
    val c6 = f6(t)
    new SelectQuery[T, (A, B, C, D, E, F)](t,
      QueryBuilder.select(
        c1.col.name,
        c2.col.name,
        c3.col.name,
        c4.col.name,
        c5.col.name,
        c6.col.name
      ).from(tableName), r => (c1(r), c2(r), c3(r), c4(r), c5(r), c6(r)))
  }

  def select[A, B, C, D, E, F, G](
    f1: T =>SelectColumn[A],
    f2: T => SelectColumn[B],
    f3: T => SelectColumn[C],
    f4: T => SelectColumn[D],
    f5: T => SelectColumn[E],
    f6: T => SelectColumn[F],
    f7: T => SelectColumn[G]
    ): SelectQuery[T, (A, B, C, D, E, F, G)] = {
    val t = this.asInstanceOf[T]
    val c1 = f1(t)
    val c2 = f2(t)
    val c3 = f3(t)
    val c4 = f4(t)
    val c5 = f5(t)
    val c6 = f6(t)
    val c7 = f7(t)
    new SelectQuery[T, (A, B, C, D, E, F, G)](t,
      QueryBuilder.select(
        c1.col.name,
        c2.col.name,
        c3.col.name,
        c4.col.name,
        c5.col.name,
        c6.col.name,
        c7.col.name
      ).from(tableName), r => (c1(r), c2(r), c3(r), c4(r), c5(r), c6(r), c7(r)))
  }

  /**
   * Select method for 8 records.
   */
  def select[A, B, C, D, E, F, G, H](
    f1: T =>SelectColumn[A],
    f2: T => SelectColumn[B],
    f3: T => SelectColumn[C],
    f4: T => SelectColumn[D],
    f5: T => SelectColumn[E],
    f6: T => SelectColumn[F],
    f7: T => SelectColumn[G],
    f8: T => SelectColumn[H]
    ): SelectQuery[T, (A, B, C, D, E, F, G, H)] = {
    val t = this.asInstanceOf[T]
    val c1 = f1(t)
    val c2 = f2(t)
    val c3 = f3(t)
    val c4 = f4(t)
    val c5 = f5(t)
    val c6 = f6(t)
    val c7 = f7(t)
    val c8 = f8(t)
    new SelectQuery[T, (A, B, C, D, E, F, G, H)](t,
      QueryBuilder.select(
        c1.col.name,
        c2.col.name,
        c3.col.name,
        c4.col.name,
        c5.col.name,
        c6.col.name,
        c7.col.name,
        c8.col.name
      ).from(tableName), r => (c1(r), c2(r), c3(r), c4(r), c5(r), c6(r), c7(r), c8(r)))
  }

  /**
   * Select method for 9 records.
   */
  def select[A, B, C, D, E, F, G, H, I](
    f1: T =>SelectColumn[A],
    f2: T => SelectColumn[B],
    f3: T => SelectColumn[C],
    f4: T => SelectColumn[D],
    f5: T => SelectColumn[E],
    f6: T => SelectColumn[F],
    f7: T => SelectColumn[G],
    f8: T => SelectColumn[H],
    f9: T => SelectColumn[I]
    ): SelectQuery[T, (A, B, C, D, E, F, G, H, I)] = {
    val t = this.asInstanceOf[T]
    val c1 = f1(t)
    val c2 = f2(t)
    val c3 = f3(t)
    val c4 = f4(t)
    val c5 = f5(t)
    val c6 = f6(t)
    val c7 = f7(t)
    val c8 = f8(t)
    val c9 = f9(t)
    new SelectQuery[T, (A, B, C, D, E, F, G, H, I)](t,
      QueryBuilder.select(
        c1.col.name,
        c2.col.name,
        c3.col.name,
        c4.col.name,
        c5.col.name,
        c6.col.name,
        c7.col.name,
        c8.col.name,
        c9.col.name
      ).from(tableName), r => (c1(r), c2(r), c3(r), c4(r), c5(r), c6(r), c7(r), c8(r), c9(r)))
  }

  /**
   * Select method for 10 records.
   */
  def select[A1, A2, A3, A4, A5, A6, A7, A8, A9, A10](
    f1: T =>SelectColumn[A1],
    f2: T => SelectColumn[A2],
    f3: T => SelectColumn[A3],
    f4: T => SelectColumn[A4],
    f5: T => SelectColumn[A5],
    f6: T => SelectColumn[A6],
    f7: T => SelectColumn[A7],
    f8: T => SelectColumn[A8],
    f9: T => SelectColumn[A9],
    f10: T => SelectColumn[A10]
    ): SelectQuery[T, (A1, A2, A3, A4, A5, A6, A7, A8, A9, A10)] = {
    val t = this.asInstanceOf[T]
    val c1 = f1(t)
    val c2 = f2(t)
    val c3 = f3(t)
    val c4 = f4(t)
    val c5 = f5(t)
    val c6 = f6(t)
    val c7 = f7(t)
    val c8 = f8(t)
    val c9 = f9(t)
    val c10 = f10(t)
    new SelectQuery[T, (A1, A2, A3, A4, A5, A6, A7, A8, A9, A10)](t,
      QueryBuilder.select(
        c1.col.name,
        c2.col.name,
        c3.col.name,
        c4.col.name,
        c5.col.name,
        c6.col.name,
        c7.col.name,
        c8.col.name,
        c9.col.name,
        c9.col.name
      ).from(tableName), r => (c1(r), c2(r), c3(r), c4(r), c5(r), c6(r), c7(r), c8(r), c9(r), c10(r)))
  }

  def distinct[A](f1: T => SelectColumn[A]): SelectQuery[T, A] = {
    val t = this.asInstanceOf[T]
    val c = f1(t)
    new SelectQuery[T, A](t, QueryBuilder.select.distinct().from(tableName), c.apply)
  }

}
