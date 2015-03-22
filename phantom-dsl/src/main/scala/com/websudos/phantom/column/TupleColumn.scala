package com.websudos.phantom.column

import com.websudos.phantom.CassandraTable
import com.websudos.phantom.builder.primitives.Primitive

class Tuple2Column[T <: CassandraTable[T, R], R, R1 : Primitive, R2: Primitive] extends PrimitiveColumn[T, R] {

}
