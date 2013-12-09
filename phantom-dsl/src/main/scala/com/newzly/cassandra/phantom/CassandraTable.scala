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
package com
package newzly
package cassandra
package phantom

import java.io.Serializable

import com.datastax.driver.core.Row
import com.datastax.driver.core.querybuilder._
import scala.reflect.runtime.universe._
import com.newzly.cassandra.phantom.query.{
  DeleteQuery,
  InsertQuery,
  SelectQuery,
  UpdateQuery
}
import scala.reflect.ClassTag

abstract class CassandraTable[T <: CassandraTable[T, R], R] {

  lazy val tableName: String = this.getClass.getSimpleName

  def fromRow(r: Row): R

  def column[RR: CassandraPrimitive]: PrimitiveColumn[RR] =
    new PrimitiveColumn[RR]()

  def optColumn[RR: CassandraPrimitive]: OptionalPrimitiveColumn[RR] =
    new OptionalPrimitiveColumn[RR]

  def jsonColumn[RR: Manifest]: JsonTypeColumn[RR] =
    new JsonTypeColumn[RR]

  def enumColumn[EnumType <: Enumeration](enum: EnumType): EnumColumn[EnumType] =
    new EnumColumn[EnumType](enum)

  def column[S <: Seq[RR], RR: CassandraPrimitive]: SeqColumnNew[S, RR] =
    new SeqColumnNew[S, RR]

  def setColumn[RR: CassandraPrimitive]: SetColumn[RR] =
    new SetColumn[RR]

  def seqColumn[RR: CassandraPrimitive]: SeqColumn[RR] =
    new SeqColumn[RR]()

  def column[Map, K: CassandraPrimitive, V: CassandraPrimitive] =
    new MapColumn[K, V]()

  def mapColumn[K: CassandraPrimitive, V: CassandraPrimitive] =
    new MapColumn[K, V]()

  def jsonSeqColumn[RR: Manifest]: JsonTypeSeqColumn[RR] =
    new JsonTypeSeqColumn[RR]()

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

  def update = new UpdateQuery[T, R](this.asInstanceOf[T], QueryBuilder.update(tableName))

  def insert = new InsertQuery[T, R](this.asInstanceOf[T], QueryBuilder.insertInto(tableName))

  def delete = new DeleteQuery[T, R](this.asInstanceOf[T], QueryBuilder.delete.from(tableName))

}
