package com.websudos.phantom.builder.query

import com.datastax.driver.core.{ColumnMetadata, TableMetadata}
import com.websudos.phantom.CassandraTable
import com.websudos.phantom.builder.QueryBuilder
import com.websudos.phantom.column.AbstractColumn
import com.websudos.phantom.connectors.KeySpace
import com.websudos.phantom.dsl.Session

import scala.collection.JavaConverters._
import scala.concurrent.{ExecutionContext, blocking}


sealed case class ColumnDiff(name: String, cassandraType: String, isPrimary: Boolean, isSecondary: Boolean, isStatic: Boolean)
sealed case class Migration(additions: Set[ColumnDiff], deletions: Set[ColumnDiff]) {

  def additiveQueries(table: CassandraTable[_, _])
  (implicit session: Session, keySpace: KeySpace, executionContext: ExecutionContext): Set[CQLQuery] = {
    additions map {
      col: ColumnDiff => {
        table.alter.add(col.name, col.cassandraType).qb
      }
    }
  }

  def subtractionQueries(table: CassandraTable[_, _])
                     (implicit session: Session, keySpace: KeySpace, executionContext: ExecutionContext): Set[CQLQuery] = {
    deletions map {
      col: ColumnDiff => {
        table.alter.drop(col.name).qb
      }
    }
  }

  def queryList(table: CassandraTable[_, _])
                        (implicit session: Session, keySpace: KeySpace, executionContext: ExecutionContext): Set[CQLQuery] = {
    additiveQueries(table) ++ subtractionQueries(table)
  }

  def automigrate(table: CassandraTable[_, _])
                 (implicit session: Session, keySpace: KeySpace, executionContext: ExecutionContext): ExecutableStatementList = {
    new ExecutableStatementList(queryList(table).toSeq)
  }
}

sealed case class TableDiff(columns: Set[ColumnDiff], table: String) {

  final def diff(other: TableDiff): TableDiff = {
    TableDiff(columns.filterNot(item => other.columns.exists(_.name == item.name)), s"$table - ${other.table}")
  }

  def hasPrimaryPart: Boolean = {
    columns.exists(_.isPrimary)
  }

  def indexes()(implicit keySpace: KeySpace): Set[CQLQuery] = {
    columns.filter(_.isSecondary).map {
      origin => {
        CQLQuery(s"CREATE INDEX IF NOT EXISTS ${origin.name} on ${QueryBuilder.keyspace(keySpace.name, table).queryString}")
      }
    }
  }

  def migrations(): Set[ColumnDiff] = {
    columns.filter {
      col => {
        if (col.isPrimary) {
          throw new Exception(s"Cannot automatically migrate PRIMARY_KEY part ${col.name}")
        } else {
          true
        }
      }
    }
  }

}

object TableDiff {

  private[this] def contains(column: ColumnMetadata, clustering: List[String]): Boolean = {
    clustering.exists(column.getName ==)
  }

  def apply(metadata: TableMetadata): TableDiff = {

    val primary = metadata.getPrimaryKey.asScala.map(_.getName).toList

    val columns = metadata.getColumns.asScala.toSet.foldLeft(Set.empty[ColumnDiff])((acc, item) => {
      acc + ColumnDiff(
        item.getName,
        item.getType.getName.toString,
        contains(item, primary),
        item.isStatic,
        item.isStatic
      )
    })

    TableDiff(columns, metadata.getName)
  }

  def apply(table: CassandraTable[_, _]): TableDiff = {
    val cols = table.columns.toSet[AbstractColumn[_]].map {
      column => {
        ColumnDiff(
          column.name,
          column.cassandraType,
          column.isClusteringKey,
          column.isSecondaryKey,
          column.isStaticColumn
        )
      }
    }
    TableDiff(cols, table.tableName)
  }
}

object Migration {
  def apply(metadata: TableMetadata, table: CassandraTable[_, _]): Migration = {

    val dbTable = TableDiff(metadata)
    val phantomTable = TableDiff(table)

    Migration(
      phantomTable diff dbTable migrations(),
      dbTable diff phantomTable migrations()
    )

  }
}

private[phantom] object SchemaAutoDiffer {

  def metadata(tableName: String)(implicit session: Session, keySpace: KeySpace): TableMetadata = {
    blocking {
      session.getCluster.getMetadata.getKeyspace(keySpace.name).getTable(tableName)
    }
  }

  def queryList(table: CassandraTable[_, _])(implicit session: Session, keySpace: KeySpace, ec: ExecutionContext): Set[CQLQuery] = {
    Migration(metadata(table.tableName), table).queryList(table)
  }

  def automigrate(table: CassandraTable[_, _])(implicit session: Session, keySpace: KeySpace, ec: ExecutionContext): ExecutableStatementList = {
    new ExecutableStatementList(queryList(table))
  }
}