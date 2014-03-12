package com.newzly.phantom.thrift

import scala.collection.JavaConverters._
import com.newzly.phantom.{CassandraTable, CassandraPrimitive}
import com.newzly.phantom.Implicits._
import com.newzly.phantom.column._
import com.twitter.scrooge.ThriftStruct
import com.datastax.driver.core.querybuilder.{QueryBuilder, Assignment}

object Implicits {

  type ThriftColumn[T <: CassandraTable[T, R], R, Value <: ThriftStruct] = com.newzly.phantom.thrift.ThriftColumn[T, R, Value]
  type ThriftSetColumn[T <: CassandraTable[T, R], R, Value <: ThriftStruct] = com.newzly.phantom.thrift.ThriftSetColumn[T, R, Value]
  type ThriftListColumn[T <: CassandraTable[T, R], R, Value <: ThriftStruct] = com.newzly.phantom.thrift.ThriftListColumn[T, R, Value]
  type ThriftMapColumn[T <: CassandraTable[T, R], R, Key, Value <: ThriftStruct] = com.newzly.phantom.thrift.ThriftMapColumn[T, R, Key, Value]
  type OptionalThriftColumn[T <: CassandraTable[T, R], R, RR <: ThriftStruct] = com.newzly.phantom.thrift.OptionalThriftColumn[T, R, RR]

  implicit class ThriftModifyColumn[T <: CassandraTable[T, R], R, RR <: ThriftStruct](col: ThriftColumn[T, R, RR]) extends AbstractModifyColumn[RR](col.name) {
    def toCType(v: RR): AnyRef = col.toCType(v)
  }

  implicit class ThriftSetLikeModifyColumn[Owner <: CassandraTable[Owner, Record], Record, RR <: ThriftStruct](col: ThriftSetColumn[Owner, Record, RR]) extends ModifyColumn[Set[RR]](col) {
    def add(value: RR): Assignment = QueryBuilder.add(col.name, col.itemToCType(value))
    def addAll(values: Set[RR]): Assignment = QueryBuilder.addAll(col.name, values.map(col.itemToCType).asJava)
    def remove(value: RR): Assignment = QueryBuilder.remove(col.name, col.itemToCType(value))
    def removeAll(values: Set[RR]): Assignment = QueryBuilder.removeAll(col.name, values.map(col.itemToCType).asJava)
  }

  implicit class ThriftListLikeModifyColumn[Owner <: CassandraTable[Owner, Record], Record, RR <: ThriftStruct](col: ThriftListColumn[Owner, Record, RR]) extends ModifyColumn[List[RR]](col) {
    def prepend(value: RR): Assignment = QueryBuilder.prepend(col.name, col.itemToCType(value))
    def prependAll(values: List[RR]): Assignment = QueryBuilder.prependAll(col.name, values.map(col.itemToCType).asJava)
    def append(value: RR): Assignment = QueryBuilder.append(col.name, col.itemToCType(value))
    def appendAll(values: List[RR]): Assignment = QueryBuilder.appendAll(col.name, values.map(col.itemToCType).asJava)
    def remove(value: RR): Assignment = QueryBuilder.remove(col.name, col.itemToCType(value))
    def removeAll(values: List[RR]): Assignment = QueryBuilder.removeAll(col.name, values.toSet[RR].map(col.itemToCType).asJava)
  }

  implicit class ThriftMapLikeModifyColumn[Owner <: CassandraTable[Owner, Record], Record, Key : CassandraPrimitive, RR <: ThriftStruct](col: ThriftMapColumn[Owner, Record, Key, RR]) extends ModifyColumn[Map[Key, RR]](col) {
    def put(value: (Key, RR)): Assignment = QueryBuilder.put(col.name, CassandraPrimitive[Key].toCType(value._1), col.itemToCType(value._2))
    def putAll[L <% Traversable[(Key, RR)]](values: L): Assignment = {
      val map = values.map({ case (k, v) => CassandraPrimitive[Key].toCType(k) -> col.itemToCType(v).asInstanceOf[String] }).toMap.asJava
      QueryBuilder.putAll(col.name, map)
    }
  }
}
