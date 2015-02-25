package com.websudos.phantom.builder.query

import com.twitter.util.StorageUnit
import com.websudos.phantom.CassandraTable
import com.websudos.phantom.builder._

sealed trait WithBound
sealed trait WithChainned extends WithBound
sealed trait WithUnchainned extends WithBound

sealed class WithClause(val qb: CQLQuery) {}

sealed class EscapableWithClause(override val qb: CQLQuery, escaped : Boolean = false) extends WithClause(qb) {

  private[this] def escapeIfUnescaped(): EscapableWithClause = {
    if (escaped) {
      this
    } else {
      new EscapableWithClause(QueryBuilder.escapeOptions(qb), true)
    }
  }

}

trait WithClauses {
  object Storage {
    case object CompactStorage extends WithClause(CQLQuery(CQLSyntax.StorageMechanisms.CompactStorage))
  }

  object Compression {
    sealed class BuilderClause(val qb: CQLQuery) {

    }
  }

  object Compaction {

    private[this] def rootStrategy(strategy: String) = {
      CQLQuery.empty.append(CQLSyntax.CompactionOptions.`class`)
        .forcePad.append(CQLSyntax.Symbols.`:`)
        .append(strategy)
    }

    sealed class SizeTieredCompactionStrategy(qb: CQLQuery) extends WithClause(qb) {
      def min_sstable_size(unit: StorageUnit): SizeTieredCompactionStrategy = {
        new SizeTieredCompactionStrategy(QueryBuilder.min_sstable_size(qb, unit.toHuman()))
      }

      def sstable_size_in_mb(unit: StorageUnit): SizeTieredCompactionStrategy = {
        new SizeTieredCompactionStrategy(QueryBuilder.sstable_size_in_mb(qb, unit.toHuman()))
      }

      def bucket_high(size: Double): SizeTieredCompactionStrategy = {
        new SizeTieredCompactionStrategy(QueryBuilder.bucket_high(qb, size))
      }

      def bucket_low(size: Double): SizeTieredCompactionStrategy = {
        new SizeTieredCompactionStrategy(QueryBuilder.bucket_low(qb, size))
      }
    }


    case object SizeTieredCompactionStrategy extends SizeTieredCompactionStrategy(rootStrategy(CQLSyntax.CompactionStrategies.SizeTieredCompactionStrategy))
  }
}

object WithClauses extends WithClauses

class RootCreateQuery[
  Table <: CassandraTable[Table, _],
  Record
](val table: Table, val qb: CQLQuery) {

  private[phantom] def default: CQLQuery = {
    CQLQuery(CQLSyntax.create).forcePad.append(CQLSyntax.table)
      .forcePad.append(table.tableName).forcePad
      .append(CQLSyntax.Symbols.`(`)
      .append(QueryBuilder.join(table.columns.map(_.qb): _*))
      .append(CQLSyntax.Symbols.`,`)
      .forcePad.append(table.defineTableKey())
      .append(CQLSyntax.Symbols.`)`)
  }

  def ifNotExists: CQLQuery = {
    CQLQuery(CQLSyntax.create)
  }
}


class CreateQuery[
  Table <: CassandraTable[Table, _],
  Record,
  Status <: ConsistencyBound,
  Chain <: WithBound
](table: Table, val qb: CQLQuery) extends ExecutableStatement {

  type Default = CreateQuery[Table, Record, Unspecified, WithUnchainned]

  def `with`(clause: WithClause): CreateQuery[Table, Record, Status, WithChainned] = {
    new CreateQuery(table, QueryBuilder.`with`(qb, clause.qb))
  }

  def and(clause: WithClause): CreateQuery[Table, Record, Status, WithChainned] = {
    new CreateQuery(table, QueryBuilder.and(qb, clause.qb))
  }
}

private[phantom] trait CreateImplicits {
  implicit def rootCreateQueryToCreateQuery[T <: CassandraTable[T, _], R](root: RootCreateQuery[T, R]): CreateQuery[T, R, Unspecified, WithUnchainned]#Default = {
    new CreateQuery(root.table, root.default)
  }
}
