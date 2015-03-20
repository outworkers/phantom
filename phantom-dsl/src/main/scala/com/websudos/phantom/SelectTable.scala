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
package com.websudos.phantom

import com.websudos.phantom.builder.QueryBuilder
import com.websudos.phantom.builder.query.SelectQuery
import com.websudos.phantom.column.SelectColumn

trait SelectTable[T <: CassandraTable[T, _], R] {
  self: CassandraTable[T, R] =>

  def select: SelectQuery.Default[T, R] = SelectQuery[T, R](this.asInstanceOf[T], QueryBuilder.select(tableName), fromRow)

  def select[A](f1: T => SelectColumn[A]): SelectQuery.Default[T, A] = {
    val t = this.asInstanceOf[T]
    val c = f1(t)
    SelectQuery(t, QueryBuilder.select(tableName, c.col.name), c.apply)
  }

  def select[A, B](f1: T => SelectColumn[A], f2: T => SelectColumn[B]): SelectQuery.Default[T, (A, B)] = {
    val t = this.asInstanceOf[T]
    val c1 = f1(t)
    val c2 = f2(t)
    SelectQuery[T, (A, B)](t, QueryBuilder.select(tableName, c1.col.name, c2.col.name), r => (c1(r), c2(r)))
  }

  def select[A, B, C](f1: T => SelectColumn[A], f2: T => SelectColumn[B], f3: T => SelectColumn[C]): SelectQuery.Default[T, (A, B, C)] = {
    val t = this.asInstanceOf[T]
    val c1 = f1(t)
    val c2 = f2(t)
    val c3 = f3(t)
    SelectQuery[T, (A, B, C)](t, QueryBuilder.select(tableName, c1.col.name, c2.col.name, c3.col.name), r => (c1(r), c2(r), c3(r)))
  }

  def select[A, B, C, D](
    f1: T =>SelectColumn[A],
   f2: T => SelectColumn[B],
   f3: T => SelectColumn[C],
   f4: T => SelectColumn[D]): SelectQuery.Default[T, (A, B, C, D)] = {
    val t = this.asInstanceOf[T]
    val c1 = f1(t)
    val c2 = f2(t)
    val c3 = f3(t)
    val c4 = f4(t)
    SelectQuery[T, (A, B, C, D)](t,
      QueryBuilder.select(tableName, c1.col.name, c2.col.name, c3.col.name, c4.col.name),
      r => (c1(r), c2(r), c3(r), c4(r))
    )
  }

  def select[A, B, C, D, E](f1: T =>SelectColumn[A], f2: T => SelectColumn[B], f3: T => SelectColumn[C], f4: T => SelectColumn[D], f5: T => SelectColumn[E]): SelectQuery.Default[T, (A, B, C, D, E)] = {
    val t = this.asInstanceOf[T]
    val c1 = f1(t)
    val c2 = f2(t)
    val c3 = f3(t)
    val c4 = f4(t)
    val c5 = f5(t)
    SelectQuery[T, (A, B, C, D, E)](t, QueryBuilder.select(tableName, c1.col.name, c2.col.name, c3.col.name, c4.col.name, c5.col.name), r => (c1(r), c2(r), c3(r), c4(r), c5(r)))
  }

  def select[A, B, C, D, E, F](
    f1: T =>SelectColumn[A],
    f2: T => SelectColumn[B],
    f3: T => SelectColumn[C],
    f4: T => SelectColumn[D],
    f5: T => SelectColumn[E],
    f6: T => SelectColumn[F]): SelectQuery.Default[T, (A, B, C, D, E, F)] = {
    val t = this.asInstanceOf[T]
    val c1 = f1(t)
    val c2 = f2(t)
    val c3 = f3(t)
    val c4 = f4(t)
    val c5 = f5(t)
    val c6 = f6(t)
    SelectQuery[T, (A, B, C, D, E, F)](t,
      QueryBuilder.select(
        tableName,
        c1.col.name,
        c2.col.name,
        c3.col.name,
        c4.col.name,
        c5.col.name,
        c6.col.name
      ), r => (c1(r), c2(r), c3(r), c4(r), c5(r), c6(r)))
  }

  def select[A, B, C, D, E, F, G](
    f1: T =>SelectColumn[A],
    f2: T => SelectColumn[B],
    f3: T => SelectColumn[C],
    f4: T => SelectColumn[D],
    f5: T => SelectColumn[E],
    f6: T => SelectColumn[F],
    f7: T => SelectColumn[G]
    ): SelectQuery.Default[T, (A, B, C, D, E, F, G)] = {
    val t = this.asInstanceOf[T]
    val c1 = f1(t)
    val c2 = f2(t)
    val c3 = f3(t)
    val c4 = f4(t)
    val c5 = f5(t)
    val c6 = f6(t)
    val c7 = f7(t)
    SelectQuery[T, (A, B, C, D, E, F, G)](t,
      QueryBuilder.select(
        tableName,
        c1.col.name,
        c2.col.name,
        c3.col.name,
        c4.col.name,
        c5.col.name,
        c6.col.name,
        c7.col.name
      ), r => (c1(r), c2(r), c3(r), c4(r), c5(r), c6(r), c7(r)))
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
    ): SelectQuery.Default[T, (A, B, C, D, E, F, G, H)] = {
    val t = this.asInstanceOf[T]
    val c1 = f1(t)
    val c2 = f2(t)
    val c3 = f3(t)
    val c4 = f4(t)
    val c5 = f5(t)
    val c6 = f6(t)
    val c7 = f7(t)
    val c8 = f8(t)
    SelectQuery[T, (A, B, C, D, E, F, G, H)](t,
      QueryBuilder.select(
        tableName,
        c1.col.name,
        c2.col.name,
        c3.col.name,
        c4.col.name,
        c5.col.name,
        c6.col.name,
        c7.col.name,
        c8.col.name
      ), r => (c1(r), c2(r), c3(r), c4(r), c5(r), c6(r), c7(r), c8(r)))
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
    ): SelectQuery.Default[T, (A, B, C, D, E, F, G, H, I)] = {
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
    SelectQuery[T, (A, B, C, D, E, F, G, H, I)](t,
      QueryBuilder.select(
        tableName,
        c1.col.name,
        c2.col.name,
        c3.col.name,
        c4.col.name,
        c5.col.name,
        c6.col.name,
        c7.col.name,
        c8.col.name,
        c9.col.name
      ), r => (c1(r), c2(r), c3(r), c4(r), c5(r), c6(r), c7(r), c8(r), c9(r)))
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
    ): SelectQuery.Default[T, (A1, A2, A3, A4, A5, A6, A7, A8, A9, A10)] = {
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
    SelectQuery[T, (A1, A2, A3, A4, A5, A6, A7, A8, A9, A10)](t,
      QueryBuilder.select(
        tableName,
        c1.col.name,
        c2.col.name,
        c3.col.name,
        c4.col.name,
        c5.col.name,
        c6.col.name,
        c7.col.name,
        c8.col.name,
        c9.col.name,
        c10.col.name
      ), r => (c1(r), c2(r), c3(r), c4(r), c5(r), c6(r), c7(r), c8(r), c9(r), c10(r)))
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
  f11: T => SelectColumn[A11]): SelectQuery.Default[T, (A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11)] = {
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
 SelectQuery[T, (A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11)](t, QueryBuilder.select(
   tableName,
   c1.col.name,
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
), r => (c1(r), c2(r), c3(r), c4(r), c5(r), c6(r), c7(r), c8(r), c9(r), c10(r), c11(r)))}

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
f12: T => SelectColumn[A12]) : SelectQuery.Default[T, (A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12)] = {
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

SelectQuery[T, (A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12)](t, QueryBuilder.select(
  tableName,
  c1.col.name,
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
),
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
f13: T => SelectColumn[A13]) : SelectQuery.Default[T, (A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13)] = {
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

SelectQuery[T, (A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13)](t, QueryBuilder.select(
  tableName,
  c1.col.name,
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
),
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
  f14: T => SelectColumn[A14]
) : SelectQuery.Default[T, (A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14)] = {
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
SelectQuery[T, (A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14)](t, QueryBuilder.select(
  tableName,
  c1.col.name,
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
),
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
  f15: T => SelectColumn[A15]
) : SelectQuery.Default[T, (A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15)] = {
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
 SelectQuery[T, (A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15)](t, QueryBuilder.select(tableName,
  c1.col.name,
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
),
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
f16: T => SelectColumn[A16]): SelectQuery.Default[T, (A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16)] = {
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
SelectQuery[T, (A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16)](t, QueryBuilder.select(
  tableName,
  c1.col.name,
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
),
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
f17: T => SelectColumn[A17]) : SelectQuery.Default[T, (A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17)] = {
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
SelectQuery[T, (A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17)](t, QueryBuilder.select(
  tableName,
  c1.col.name,
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
),
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
  f18: T => SelectColumn[A18]
) : SelectQuery.Default[T, (A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17, A18)] = {
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
SelectQuery[T, (A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17, A18)](t, QueryBuilder.select(
  tableName,
  c1.col.name,
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
), r => (c1(r), c2(r), c3(r), c4(r), c5(r), c6(r), c7(r), c8(r), c9(r), c10(r), c11(r), c12(r), c13(r), c14(r), c15(r), c16(r), c17(r), c18(r)))}

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
  f19: T => SelectColumn[A19]
) : SelectQuery.Default[T, (A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17, A18, A19)] = {
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
SelectQuery[T, (A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17, A18, A19)](t, QueryBuilder.select(
  tableName,
  c1.col.name,
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
),
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
  f20: T => SelectColumn[A20]
) : SelectQuery.Default[T, (A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17, A18, A19, A20)] = {
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
SelectQuery[T, (A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17, A18, A19, A20)](t, QueryBuilder.select(
  tableName,
  c1.col.name,
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
),
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
f21: T => SelectColumn[A21]) : SelectQuery.Default[T, (A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17, A18, A19, A20, A21)] = {
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
SelectQuery[T, (A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17, A18, A19, A20, A21)](t, QueryBuilder.select(
  tableName,
  c1.col.name,
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
),
r => (c1(r), c2(r), c3(r), c4(r), c5(r), c6(r), c7(r), c8(r), c9(r), c10(r), c11(r), c12(r), c13(r), c14(r), c15(r), c16(r), c17(r), c18(r), c19(r), c20(r), c21(r)))}

  def distinct[A](f1: T => SelectColumn[A]): SelectQuery.Default[T, A] = {
    val t = this.asInstanceOf[T]
    val c = f1(t)
    SelectQuery[T, A](t, QueryBuilder.select(tableName, c.col.name).distinct(), c.apply)
  }

  def distinct[A1, A2](f1: T => SelectColumn[A1], f2: T => SelectColumn[A2]): SelectQuery.Default[T, (A1, A2)] = {
    val t = this.asInstanceOf[T]
    val (c1, c2) = (f1(t), f2(t))
    SelectQuery[T, (A1, A2)](t, QueryBuilder.select.column(c1.col.name).column(c2.col.name).distinct().from(tableName), r => (c1(r), c2(r)))
  }

}
