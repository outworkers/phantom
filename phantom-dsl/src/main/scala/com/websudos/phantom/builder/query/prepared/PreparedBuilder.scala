package com.websudos.phantom.builder.query.prepared

import com.websudos.phantom.CassandraTable
import com.websudos.phantom.builder.query._
import com.websudos.phantom.connectors.KeySpace

sealed class PreparedBuilder[T <: CassandraTable[T, _], R](table: T)(implicit keySpace: KeySpace) {


  final def update()(implicit keySpace: KeySpace): PreparedUpdateQuery.Default[T, R] = PreparedUpdateQuery(table)

  final def insert()(implicit keySpace: KeySpace): InsertQuery.Default[T, R] = InsertQuery(table)

  final def delete()(implicit keySpace: KeySpace): DeleteQuery.Default[T, R] = DeleteQuery(table)

  final def truncate()(implicit keySpace: KeySpace): TruncateQuery.Default[T, R] = TruncateQuery(table)
}
