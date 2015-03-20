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
package com.websudos.phantom.column

import com.datastax.driver.core.Row
import com.datastax.driver.core.querybuilder.{Assignment, QueryBuilder}
import com.websudos.phantom.CassandraTable
import com.websudos.phantom.keys.{ClusteringOrder, Index, PartitionKey, PrimaryKey}

import scala.annotation.implicitNotFound
import scala.collection.generic.CanBuildFrom
import scala.collection.immutable
import scala.collection.immutable.WrappedString
import scala.collection.mutable.WrappedArray
import scala.runtime._


private[phantom] abstract class AbstractModifyColumn[RR](name: String) {

  def toCType(v: RR): AnyRef

  def setTo(value: RR): Assignment = QueryBuilder.set(name, toCType(value))
}

sealed abstract class SelectColumn[T](val col: AbstractColumn[_]) {
  def apply(r: Row): T
}

class ModifyColumn[RR](col: AbstractColumn[RR]) extends AbstractModifyColumn[RR](col.name) {

  def toCType(v: RR): AnyRef = col.toCType(v)
}


/** The `LowPriorityImplicits` class provides implicit values that
 *  are valid in all Scala compilation units without explicit qualification,
 *  but that are partially overridden by higher-priority conversions in object
 *  `Predef`.
 *
 *  @author  Martin Odersky
 *  @since 2.8
 */
class LowPriorityImplicits {
  /** We prefer the java.lang.* boxed types to these wrappers in
   *  any potential conflicts.  Conflicts do exist because the wrappers
   *  need to implement ScalaNumber in order to have a symmetric equals
   *  method, but that implies implementing java.lang.Number as well.
   */
  implicit def byteWrapper(x: Byte): RichByte = new runtime.RichByte(x)
  implicit def shortWrapper(x: Short): RichShort = new runtime.RichShort(x)
  implicit def intWrapper(x: Int): RichInt = new runtime.RichInt(x)
  implicit def charWrapper(c: Char): RichChar = new runtime.RichChar(c)
  implicit def longWrapper(x: Long): RichLong = new runtime.RichLong(x)
  implicit def floatWrapper(x: Float): RichFloat = new runtime.RichFloat(x)
  implicit def doubleWrapper(x: Double): RichDouble = new runtime.RichDouble(x)
  implicit def booleanWrapper(x: Boolean): RichBoolean = new runtime.RichBoolean(x)
  
  // These eight implicits exist solely to exclude Null from the domain of
  // the boxed types, so that e.g. "var x: Int = null" is a compile time
  // error rather than a delayed null pointer exception by way of the
  // conversion from java.lang.Integer.  If defined in the same file as
  // Integer2int, they would have higher priority because Null is a subtype
  // of Integer.  We balance that out and create conflict by moving the
  // definition into the superclass.
  //
  // Caution: do not adjust tightrope tension without safety goggles in place.
  implicit def Byte2byteNullConflict(x: Null): Byte          = sys.error("value error")
  implicit def Short2shortNullConflict(x: Null): Short       = sys.error("value error")
  implicit def Character2charNullConflict(x: Null): Char     = sys.error("value error")
  implicit def Integer2intNullConflict(x: Null): Int         = sys.error("value error")
  implicit def Long2longNullConflict(x: Null): Long          = sys.error("value error")
  implicit def Float2floatNullConflict(x: Null): Float       = sys.error("value error")
  implicit def Double2doubleNullConflict(x: Null): Double    = sys.error("value error")
  implicit def Boolean2booleanNullConflict(x: Null): Boolean = sys.error("value error")

  implicit def genericWrapArray[T](xs: Array[T]): WrappedArray[T] =
    if (xs eq null) null
    else WrappedArray.make(xs)

  // Since the JVM thinks arrays are covariant, one 0-length Array[AnyRef]
  // is as good as another for all T <: AnyRef.  Instead of creating 100,000,000
  // unique ones by way of this implicit, let's share one.
  implicit def wrapRefArray[T <: AnyRef](xs: Array[T]): WrappedArray[T] = {
    if (xs eq null) null
    else if (xs.length == 0) WrappedArray.empty[T]
    else new WrappedArray.ofRef[T](xs)
  }
    
  implicit def wrapIntArray(xs: Array[Int]): WrappedArray[Int] = if (xs ne null) new WrappedArray.ofInt(xs) else null
  implicit def wrapDoubleArray(xs: Array[Double]): WrappedArray[Double] = if (xs ne null) new WrappedArray.ofDouble(xs) else null
  implicit def wrapLongArray(xs: Array[Long]): WrappedArray[Long] = if (xs ne null) new WrappedArray.ofLong(xs) else null
  implicit def wrapFloatArray(xs: Array[Float]): WrappedArray[Float] = if (xs ne null) new WrappedArray.ofFloat(xs) else null
  implicit def wrapCharArray(xs: Array[Char]): WrappedArray[Char] = if (xs ne null) new WrappedArray.ofChar(xs) else null
  implicit def wrapByteArray(xs: Array[Byte]): WrappedArray[Byte] = if (xs ne null) new WrappedArray.ofByte(xs) else null
  implicit def wrapShortArray(xs: Array[Short]): WrappedArray[Short] = if (xs ne null) new WrappedArray.ofShort(xs) else null
  implicit def wrapBooleanArray(xs: Array[Boolean]): WrappedArray[Boolean] = if (xs ne null) new WrappedArray.ofBoolean(xs) else null
  implicit def wrapUnitArray(xs: Array[Unit]): WrappedArray[Unit] = if (xs ne null) new WrappedArray.ofUnit(xs) else null

  implicit def wrapString(s: String): WrappedString = if (s ne null) new WrappedString(s) else null
  implicit def unwrapString(ws: WrappedString): String = if (ws ne null) ws.self else null

  implicit def fallbackStringCanBuildFrom[T]: CanBuildFrom[String, T, immutable.IndexedSeq[T]] = 
    new CanBuildFrom[String, T, immutable.IndexedSeq[T]] { 
      def apply(from: String) = immutable.IndexedSeq.newBuilder[T]
      def apply() = immutable.IndexedSeq.newBuilder[T]
    }
}
sealed trait CollectionOperators {

  implicit class CounterModifyColumn[Owner <: CassandraTable[Owner, Record], Record](col: CounterColumn[Owner, Record]) {
    def increment(value: Long = 1L): Assignment = QueryBuilder.incr(col.name, value)
    def decrement(value: Long = 1L): Assignment = QueryBuilder.decr(col.name, value)
  }

  implicit class ListLikeModifyColumn[Owner <: CassandraTable[Owner, Record], Record, RR](col: AbstractListColumn[Owner, Record, RR])
    extends ModifyColumn[List[RR]](col) {

    def prepend(value: RR): Assignment = QueryBuilder.prepend(col.name, col.valueToCType(value))
    def prependAll[L](values: L)(implicit ev1: L => Seq[RR]): Assignment = QueryBuilder.prependAll(col.name, col.valuesToCType(values))
    def append(value: RR): Assignment = QueryBuilder.append(col.name, col.valueToCType(value))
    def appendAll[L](values: L)(implicit ev1: L => Seq[RR]): Assignment = QueryBuilder.appendAll(col.name, col.valuesToCType(values))
    def discard(value: RR): Assignment = QueryBuilder.discard(col.name, col.valueToCType(value))
    def discardAll[L](values: L)(implicit ev1: L => Seq[RR]): Assignment = QueryBuilder.discardAll(col.name, col.valuesToCType(values))
    def setIdx(i: Int, value: RR): Assignment = QueryBuilder.setIdx(col.name, i, col.valueToCType(value))
  }

  implicit class SetLikeModifyColumn[Owner <: CassandraTable[Owner, Record], Record, RR](col: AbstractSetColumn[Owner, Record, RR])
    extends ModifyColumn[Set[RR]](col) {

    def add(value: RR): Assignment = QueryBuilder.add(col.name, col.valueToCType(value))
    def addAll(values: Set[RR]): Assignment = QueryBuilder.addAll(col.name, col.valuesToCType(values))
    def remove(value: RR): Assignment = QueryBuilder.remove(col.name, col.valueToCType(value))
    def removeAll(values: Set[RR]): Assignment = QueryBuilder.removeAll(col.name, col.valuesToCType(values))
  }

  implicit class MapLikeModifyColumn[Owner <: CassandraTable[Owner, Record], Record, A, B](col: AbstractMapColumn[Owner, Record, A, B])
    extends ModifyColumn[Map[A, B]](col) {

    def put(value: (A, B)): Assignment = QueryBuilder.put(col.name, col.keyToCType(value._1), col.valueToCType(value._2))
    def putAll[L](values: L)(implicit ev1: L => Traversable[(A, B)]): Assignment = QueryBuilder.putAll(col.name, col.valuesToCType(values))
  }
}


sealed class ModifiableColumn[T]
sealed trait ModifyImplicits extends LowPriorityImplicits {

  implicit final def columnsAreModifiable[T <: AbstractColumn[_]]: ModifiableColumn[T] = new ModifiableColumn[T]

  implicit final def countersAreNotModifiable[T <: AbstractColumn[RR] with CounterRestriction[RR], RR]: ModifiableColumn[T] = new ModifiableColumn[T]
  implicit final def countersAreNotModifiable2[T <: AbstractColumn[RR] with CounterRestriction[RR], RR]: ModifiableColumn[T] = new ModifiableColumn[T]

  implicit final def primaryKeysAreNotModifiable[T <: AbstractColumn[RR] with PrimaryKey[RR], RR]: ModifiableColumn[T] = new ModifiableColumn[T]
  implicit final def primaryKeysAreNotModifiable2[T <: AbstractColumn[RR] with PrimaryKey[RR], RR]: ModifiableColumn[T] = new ModifiableColumn[T]

  implicit final def clusteringKeysAreNotModifiable[T <: AbstractColumn[RR] with ClusteringOrder[RR], RR]: ModifiableColumn[T] = new ModifiableColumn[T]
  implicit final def clusteringKeysAreNotModifiable2[T <: AbstractColumn[RR] with ClusteringOrder[RR], RR]: ModifiableColumn[T] = new ModifiableColumn[T]

  implicit final def partitionKeysAreNotModifiable[T <: AbstractColumn[RR] with PartitionKey[RR], RR]: ModifiableColumn[T] = new ModifiableColumn[T]
  implicit final def partitionKeysAreNotModifiable2[T <: AbstractColumn[RR] with PartitionKey[RR], RR]: ModifiableColumn[T] = new ModifiableColumn
  implicit final def indexesAreNotModifiable[T <: AbstractColumn[RR] with Index[RR], RR]: ModifiableColumn[T] = new ModifiableColumn[T]
  implicit final def indexesAreNotModifiable2[T <: AbstractColumn[RR] with Index[RR], RR]: ModifiableColumn[T] = new ModifiableColumn[T]

  implicit final def columnToModifyColumn[T](col: AbstractColumn[T]): ModifyColumn[T] = new ModifyColumn[T](col)

  @implicitNotFound(msg = "CounterColumns can only be incremented or decremented.")
  implicit final def nonCounterColumns[T <: CounterRestriction[RR] : ModifiableColumn, RR]
    (obj: AbstractColumn[RR] with CounterRestriction[RR]): ModifyColumn[RR] = new ModifyColumn(obj)

  @implicitNotFound(msg = "The value of primary key columns cannot be updated as per the Cassandra specification")
  implicit final def notPrimaryKeys[T <: PrimaryKey[RR] : ModifiableColumn, RR]
    (obj: AbstractColumn[RR] with PrimaryKey[RR]): ModifyColumn[RR] = new ModifyColumn(obj)

  @implicitNotFound(msg = "The value of partition key columns cannot be updated as per the Cassandra specification")
  implicit final def notPartitionKeys[T <: PartitionKey[RR] : ModifiableColumn, RR]
    (obj: AbstractColumn[RR] with PartitionKey[RR]): ModifyColumn[RR] = new ModifyColumn(obj)

  @implicitNotFound(msg = "The value of indexed columns cannot be updated as per the Cassandra specification")
  implicit final def notIndexKeys[T <: PartitionKey[RR] : ModifiableColumn, RR]
  (obj: AbstractColumn[RR] with Index[RR]): ModifyColumn[RR] = new ModifyColumn(obj)

  @implicitNotFound(msg = "The value of clustering columns cannot be updated as per the Cassandra specification")
  implicit final def notClusteringKeys[T <: ClusteringOrder[RR] : ModifiableColumn, RR]
    (obj: AbstractColumn[RR] with ClusteringOrder[RR]): ModifyColumn[RR] = new ModifyColumn(obj)

  implicit class ModifyColumnOptional[Owner <: CassandraTable[Owner, Record], Record, RR](col: OptionalColumn[Owner, Record, RR])
    extends AbstractModifyColumn[Option[RR]](col.name) {

    def toCType(v: Option[RR]): AnyRef = col.toCType(v)
  }

  class SelectColumnRequired[Owner <: CassandraTable[Owner, Record], Record, T](col: Column[Owner, Record, T]) extends SelectColumn[T](col) {
    def apply(r: Row): T = col.apply(r)
  }

  implicit def columnToSelection[Owner <: CassandraTable[Owner, Record], Record, T](column: Column[Owner, Record, T]): SelectColumnRequired[Owner, Record, T] = new SelectColumnRequired[Owner,
    Record, T](column)

  implicit class SelectColumnOptional[Owner <: CassandraTable[Owner, Record], Record, T](col: OptionalColumn[Owner, Record, T])
    extends SelectColumn[Option[T]](col) {
    def apply(r: Row): Option[T] = col.apply(r)
  }
}

private[phantom] trait Operations extends ModifyImplicits with CollectionOperators