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

  /**
 * Select method for 11 records.
 */
def select[A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11](
f1: T => SelectColumn[A1],
f2: T => SelectColumn[A2],
f3: T => SelectColumn[A3],
f4: T => SelectColumn[A4],
f5: T => SelectColumn[A5],
f6: T => SelectColumn[A6],
f7: T => SelectColumn[A7],
f8: T => SelectColumn[A8],
f9: T => SelectColumn[A9],
f10: T => SelectColumn[A10],
f11: T => SelectColumn[A11]):SelectQuery[T, (A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11)] = {
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
val c11 = f11(t)
new SelectQuery[T, (A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11)](t, QueryBuilder.select(c1.col.name,
c2.col.name,
c3.col.name,
c4.col.name,
c5.col.name,
c6.col.name,
c7.col.name,
c8.col.name,
c9.col.name,
c10.col.name,
c11.col.name
).from(tableName),
r => (c1(r), c2(r), c3(r), c4(r), c5(r), c6(r), c7(r), c8(r), c9(r), c10(r), c11(r)))}

/**
 * Select method for 12 records.
 */
def select[A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12](
f1: T => SelectColumn[A1],
f2: T => SelectColumn[A2],
f3: T => SelectColumn[A3],
f4: T => SelectColumn[A4],
f5: T => SelectColumn[A5],
f6: T => SelectColumn[A6],
f7: T => SelectColumn[A7],
f8: T => SelectColumn[A8],
f9: T => SelectColumn[A9],
f10: T => SelectColumn[A10],
f11: T => SelectColumn[A11],
f12: T => SelectColumn[A12]):SelectQuery[T, (A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12)] = {
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
val c11 = f11(t)
val c12 = f12(t)
new SelectQuery[T, (A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12)](t, QueryBuilder.select(c1.col.name,
c2.col.name,
c3.col.name,
c4.col.name,
c5.col.name,
c6.col.name,
c7.col.name,
c8.col.name,
c9.col.name,
c10.col.name,
c11.col.name,
c12.col.name
).from(tableName),
r => (c1(r), c2(r), c3(r), c4(r), c5(r), c6(r), c7(r), c8(r), c9(r), c10(r), c11(r), c12(r)))}

/**
 * Select method for 13 records.
 */
def select[A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13](
f1: T => SelectColumn[A1],
f2: T => SelectColumn[A2],
f3: T => SelectColumn[A3],
f4: T => SelectColumn[A4],
f5: T => SelectColumn[A5],
f6: T => SelectColumn[A6],
f7: T => SelectColumn[A7],
f8: T => SelectColumn[A8],
f9: T => SelectColumn[A9],
f10: T => SelectColumn[A10],
f11: T => SelectColumn[A11],
f12: T => SelectColumn[A12],
f13: T => SelectColumn[A13]):SelectQuery[T, (A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13)] = {
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
val c11 = f11(t)
val c12 = f12(t)
val c13 = f13(t)
new SelectQuery[T, (A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13)](t, QueryBuilder.select(c1.col.name,
c2.col.name,
c3.col.name,
c4.col.name,
c5.col.name,
c6.col.name,
c7.col.name,
c8.col.name,
c9.col.name,
c10.col.name,
c11.col.name,
c12.col.name,
c13.col.name
).from(tableName),
r => (c1(r), c2(r), c3(r), c4(r), c5(r), c6(r), c7(r), c8(r), c9(r), c10(r), c11(r), c12(r), c13(r)))}

/**
 * Select method for 14 records.
 */
def select[A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14](
f1: T => SelectColumn[A1],
f2: T => SelectColumn[A2],
f3: T => SelectColumn[A3],
f4: T => SelectColumn[A4],
f5: T => SelectColumn[A5],
f6: T => SelectColumn[A6],
f7: T => SelectColumn[A7],
f8: T => SelectColumn[A8],
f9: T => SelectColumn[A9],
f10: T => SelectColumn[A10],
f11: T => SelectColumn[A11],
f12: T => SelectColumn[A12],
f13: T => SelectColumn[A13],
f14: T => SelectColumn[A14]):SelectQuery[T, (A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14)] = {
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
val c11 = f11(t)
val c12 = f12(t)
val c13 = f13(t)
val c14 = f14(t)
new SelectQuery[T, (A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14)](t, QueryBuilder.select(c1.col.name,
c2.col.name,
c3.col.name,
c4.col.name,
c5.col.name,
c6.col.name,
c7.col.name,
c8.col.name,
c9.col.name,
c10.col.name,
c11.col.name,
c12.col.name,
c13.col.name,
c14.col.name
).from(tableName),
r => (c1(r), c2(r), c3(r), c4(r), c5(r), c6(r), c7(r), c8(r), c9(r), c10(r), c11(r), c12(r), c13(r), c14(r)))}

/**
 * Select method for 15 records.
 */
def select[A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15](
f1: T => SelectColumn[A1],
f2: T => SelectColumn[A2],
f3: T => SelectColumn[A3],
f4: T => SelectColumn[A4],
f5: T => SelectColumn[A5],
f6: T => SelectColumn[A6],
f7: T => SelectColumn[A7],
f8: T => SelectColumn[A8],
f9: T => SelectColumn[A9],
f10: T => SelectColumn[A10],
f11: T => SelectColumn[A11],
f12: T => SelectColumn[A12],
f13: T => SelectColumn[A13],
f14: T => SelectColumn[A14],
f15: T => SelectColumn[A15]):SelectQuery[T, (A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15)] = {
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
val c11 = f11(t)
val c12 = f12(t)
val c13 = f13(t)
val c14 = f14(t)
val c15 = f15(t)
new SelectQuery[T, (A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15)](t, QueryBuilder.select(c1.col.name,
c2.col.name,
c3.col.name,
c4.col.name,
c5.col.name,
c6.col.name,
c7.col.name,
c8.col.name,
c9.col.name,
c10.col.name,
c11.col.name,
c12.col.name,
c13.col.name,
c14.col.name,
c15.col.name
).from(tableName),
r => (c1(r), c2(r), c3(r), c4(r), c5(r), c6(r), c7(r), c8(r), c9(r), c10(r), c11(r), c12(r), c13(r), c14(r), c15(r)))}

/**
 * Select method for 16 records.
 */
def select[A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16](
f1: T => SelectColumn[A1],
f2: T => SelectColumn[A2],
f3: T => SelectColumn[A3],
f4: T => SelectColumn[A4],
f5: T => SelectColumn[A5],
f6: T => SelectColumn[A6],
f7: T => SelectColumn[A7],
f8: T => SelectColumn[A8],
f9: T => SelectColumn[A9],
f10: T => SelectColumn[A10],
f11: T => SelectColumn[A11],
f12: T => SelectColumn[A12],
f13: T => SelectColumn[A13],
f14: T => SelectColumn[A14],
f15: T => SelectColumn[A15],
f16: T => SelectColumn[A16]):SelectQuery[T, (A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16)] = {
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
val c11 = f11(t)
val c12 = f12(t)
val c13 = f13(t)
val c14 = f14(t)
val c15 = f15(t)
val c16 = f16(t)
new SelectQuery[T, (A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16)](t, QueryBuilder.select(c1.col.name,
c2.col.name,
c3.col.name,
c4.col.name,
c5.col.name,
c6.col.name,
c7.col.name,
c8.col.name,
c9.col.name,
c10.col.name,
c11.col.name,
c12.col.name,
c13.col.name,
c14.col.name,
c15.col.name,
c16.col.name
).from(tableName),
r => (c1(r), c2(r), c3(r), c4(r), c5(r), c6(r), c7(r), c8(r), c9(r), c10(r), c11(r), c12(r), c13(r), c14(r), c15(r), c16(r)))}

/**
 * Select method for 17 records.
 */
def select[A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17](
f1: T => SelectColumn[A1],
f2: T => SelectColumn[A2],
f3: T => SelectColumn[A3],
f4: T => SelectColumn[A4],
f5: T => SelectColumn[A5],
f6: T => SelectColumn[A6],
f7: T => SelectColumn[A7],
f8: T => SelectColumn[A8],
f9: T => SelectColumn[A9],
f10: T => SelectColumn[A10],
f11: T => SelectColumn[A11],
f12: T => SelectColumn[A12],
f13: T => SelectColumn[A13],
f14: T => SelectColumn[A14],
f15: T => SelectColumn[A15],
f16: T => SelectColumn[A16],
f17: T => SelectColumn[A17]):SelectQuery[T, (A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17)] = {
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
val c11 = f11(t)
val c12 = f12(t)
val c13 = f13(t)
val c14 = f14(t)
val c15 = f15(t)
val c16 = f16(t)
val c17 = f17(t)
new SelectQuery[T, (A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17)](t, QueryBuilder.select(c1.col.name,
c2.col.name,
c3.col.name,
c4.col.name,
c5.col.name,
c6.col.name,
c7.col.name,
c8.col.name,
c9.col.name,
c10.col.name,
c11.col.name,
c12.col.name,
c13.col.name,
c14.col.name,
c15.col.name,
c16.col.name,
c17.col.name
).from(tableName),
r => (c1(r), c2(r), c3(r), c4(r), c5(r), c6(r), c7(r), c8(r), c9(r), c10(r), c11(r), c12(r), c13(r), c14(r), c15(r), c16(r), c17(r)))}

/**
 * Select method for 18 records.
 */
def select[A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17, A18](
f1: T => SelectColumn[A1],
f2: T => SelectColumn[A2],
f3: T => SelectColumn[A3],
f4: T => SelectColumn[A4],
f5: T => SelectColumn[A5],
f6: T => SelectColumn[A6],
f7: T => SelectColumn[A7],
f8: T => SelectColumn[A8],
f9: T => SelectColumn[A9],
f10: T => SelectColumn[A10],
f11: T => SelectColumn[A11],
f12: T => SelectColumn[A12],
f13: T => SelectColumn[A13],
f14: T => SelectColumn[A14],
f15: T => SelectColumn[A15],
f16: T => SelectColumn[A16],
f17: T => SelectColumn[A17],
f18: T => SelectColumn[A18]):SelectQuery[T, (A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17, A18)] = {
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
val c11 = f11(t)
val c12 = f12(t)
val c13 = f13(t)
val c14 = f14(t)
val c15 = f15(t)
val c16 = f16(t)
val c17 = f17(t)
val c18 = f18(t)
new SelectQuery[T, (A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17, A18)](t, QueryBuilder.select(c1.col.name,
c2.col.name,
c3.col.name,
c4.col.name,
c5.col.name,
c6.col.name,
c7.col.name,
c8.col.name,
c9.col.name,
c10.col.name,
c11.col.name,
c12.col.name,
c13.col.name,
c14.col.name,
c15.col.name,
c16.col.name,
c17.col.name,
c18.col.name
).from(tableName),
r => (c1(r), c2(r), c3(r), c4(r), c5(r), c6(r), c7(r), c8(r), c9(r), c10(r), c11(r), c12(r), c13(r), c14(r), c15(r), c16(r), c17(r), c18(r)))}

/**
 * Select method for 19 records.
 */
def select[A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17, A18, A19](
f1: T => SelectColumn[A1],
f2: T => SelectColumn[A2],
f3: T => SelectColumn[A3],
f4: T => SelectColumn[A4],
f5: T => SelectColumn[A5],
f6: T => SelectColumn[A6],
f7: T => SelectColumn[A7],
f8: T => SelectColumn[A8],
f9: T => SelectColumn[A9],
f10: T => SelectColumn[A10],
f11: T => SelectColumn[A11],
f12: T => SelectColumn[A12],
f13: T => SelectColumn[A13],
f14: T => SelectColumn[A14],
f15: T => SelectColumn[A15],
f16: T => SelectColumn[A16],
f17: T => SelectColumn[A17],
f18: T => SelectColumn[A18],
f19: T => SelectColumn[A19]):SelectQuery[T, (A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17, A18, A19)] = {
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
val c11 = f11(t)
val c12 = f12(t)
val c13 = f13(t)
val c14 = f14(t)
val c15 = f15(t)
val c16 = f16(t)
val c17 = f17(t)
val c18 = f18(t)
val c19 = f19(t)
new SelectQuery[T, (A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17, A18, A19)](t, QueryBuilder.select(c1.col.name,
c2.col.name,
c3.col.name,
c4.col.name,
c5.col.name,
c6.col.name,
c7.col.name,
c8.col.name,
c9.col.name,
c10.col.name,
c11.col.name,
c12.col.name,
c13.col.name,
c14.col.name,
c15.col.name,
c16.col.name,
c17.col.name,
c18.col.name,
c19.col.name
).from(tableName),
r => (c1(r), c2(r), c3(r), c4(r), c5(r), c6(r), c7(r), c8(r), c9(r), c10(r), c11(r), c12(r), c13(r), c14(r), c15(r), c16(r), c17(r), c18(r), c19(r)))}

/**
 * Select method for 20 records.
 */
def select[A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17, A18, A19, A20](
f1: T => SelectColumn[A1],
f2: T => SelectColumn[A2],
f3: T => SelectColumn[A3],
f4: T => SelectColumn[A4],
f5: T => SelectColumn[A5],
f6: T => SelectColumn[A6],
f7: T => SelectColumn[A7],
f8: T => SelectColumn[A8],
f9: T => SelectColumn[A9],
f10: T => SelectColumn[A10],
f11: T => SelectColumn[A11],
f12: T => SelectColumn[A12],
f13: T => SelectColumn[A13],
f14: T => SelectColumn[A14],
f15: T => SelectColumn[A15],
f16: T => SelectColumn[A16],
f17: T => SelectColumn[A17],
f18: T => SelectColumn[A18],
f19: T => SelectColumn[A19],
f20: T => SelectColumn[A20]):SelectQuery[T, (A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17, A18, A19, A20)] = {
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
val c11 = f11(t)
val c12 = f12(t)
val c13 = f13(t)
val c14 = f14(t)
val c15 = f15(t)
val c16 = f16(t)
val c17 = f17(t)
val c18 = f18(t)
val c19 = f19(t)
val c20 = f20(t)
new SelectQuery[T, (A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17, A18, A19, A20)](t, QueryBuilder.select(c1.col.name,
c2.col.name,
c3.col.name,
c4.col.name,
c5.col.name,
c6.col.name,
c7.col.name,
c8.col.name,
c9.col.name,
c10.col.name,
c11.col.name,
c12.col.name,
c13.col.name,
c14.col.name,
c15.col.name,
c16.col.name,
c17.col.name,
c18.col.name,
c19.col.name,
c20.col.name
).from(tableName),
r => (c1(r), c2(r), c3(r), c4(r), c5(r), c6(r), c7(r), c8(r), c9(r), c10(r), c11(r), c12(r), c13(r), c14(r), c15(r), c16(r), c17(r), c18(r), c19(r), c20(r)))}

/**
 * Select method for 21 records.
 */
def select[A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17, A18, A19, A20, A21](
f1: T => SelectColumn[A1],
f2: T => SelectColumn[A2],
f3: T => SelectColumn[A3],
f4: T => SelectColumn[A4],
f5: T => SelectColumn[A5],
f6: T => SelectColumn[A6],
f7: T => SelectColumn[A7],
f8: T => SelectColumn[A8],
f9: T => SelectColumn[A9],
f10: T => SelectColumn[A10],
f11: T => SelectColumn[A11],
f12: T => SelectColumn[A12],
f13: T => SelectColumn[A13],
f14: T => SelectColumn[A14],
f15: T => SelectColumn[A15],
f16: T => SelectColumn[A16],
f17: T => SelectColumn[A17],
f18: T => SelectColumn[A18],
f19: T => SelectColumn[A19],
f20: T => SelectColumn[A20],
f21: T => SelectColumn[A21]):SelectQuery[T, (A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17, A18, A19, A20, A21)] = {
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
val c11 = f11(t)
val c12 = f12(t)
val c13 = f13(t)
val c14 = f14(t)
val c15 = f15(t)
val c16 = f16(t)
val c17 = f17(t)
val c18 = f18(t)
val c19 = f19(t)
val c20 = f20(t)
val c21 = f21(t)
new SelectQuery[T, (A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17, A18, A19, A20, A21)](t, QueryBuilder.select(c1.col.name,
c2.col.name,
c3.col.name,
c4.col.name,
c5.col.name,
c6.col.name,
c7.col.name,
c8.col.name,
c9.col.name,
c10.col.name,
c11.col.name,
c12.col.name,
c13.col.name,
c14.col.name,
c15.col.name,
c16.col.name,
c17.col.name,
c18.col.name,
c19.col.name,
c20.col.name,
c21.col.name
).from(tableName),
r => (c1(r), c2(r), c3(r), c4(r), c5(r), c6(r), c7(r), c8(r), c9(r), c10(r), c11(r), c12(r), c13(r), c14(r), c15(r), c16(r), c17(r), c18(r), c19(r), c20(r), c21(r)))}

  def distinct[A](f1: T => SelectColumn[A]): SelectQuery[T, A] = {
    val t = this.asInstanceOf[T]
    val c = f1(t)
    new SelectQuery[T, A](t, QueryBuilder.select.column(c.col.name).distinct().from(tableName), c.apply)
  }

  def distinct[A1, A2](f1: T => SelectColumn[A1], f2: T => SelectColumn[A2]): SelectQuery[T, (A1, A2)] = {
    val t = this.asInstanceOf[T]
    val (c1, c2) = (f1(t), f2(t))
    new SelectQuery[T, (A1, A2)](t, QueryBuilder.select.column(c1.col.name).column(c2.col.name).distinct().from(tableName), r => (c1(r), c2(r)))
  }

}
