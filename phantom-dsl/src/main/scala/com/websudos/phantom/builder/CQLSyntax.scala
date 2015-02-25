package com.websudos.phantom.builder

object CQLSyntax {
  val Select = "select"
  val Where = "where"
  val And = "and"
  val Or = "or"

  val Update = "update"
  val Insert = "Insert"
  val Delete = "Delete"
  val Alter = "alter"
  val Drop = "drop"
  val Describe = "describe"

  val `(` = "("

  val into = "INTO"
  val values = "VALUES"
  val select = "SELECT"
  val distinct = "DISTINCT"

  val create = "CREATE"

  val insert = "INSERT"
  val ifNotExists = "IF NOT EXISTS"
  val temporary = "TEMPORARY"

  val where = "WHERE"
  val `with` = "WHERE"
  val update = "UPDATE"
  val delete = "DELETE"
  val orderBy = "ORDER BY"
  val groupBy = "GROUP BY"
  val limit = "LIMIT"
  val and = "AND"
  val isNull = "IS NULL"
  val isNotNull = "IS NOT NULL"
  val or = "OR"
  val set = "SET"
  val from = "FROM"
  val table = "TABLE"
  val eqs = "="
  val comma = ","
  val `)` = ")"
  val asc = "ASC"
  val desc = "DESC"

  val between = "BETWEEN"
  val not = "NOT"
  val notBetween = "NOT BETWEEN"
  val exists = "EXISTS"
  val notExists = "NOT EXISTS"

  val consistency = "consistency"
  val using = "using"

  object Symbols {
    val `{` = "{"
    val `}` = "}"
    val `:` = ":"
  }

  object CompactionOptions {
    val `class` = "class"
    val max_threshold = "max_threshold"
    val min_threshold = "min_threshold"
    val min_sstable_size = "min_sstable_size"
    val sstable_size_in_mb = "sstable_size_in_mb"
    val tombstone_compaction_interval = "tombstone_compaction_interval"
    val tombstone_threshold = "tombstone_threshold"
    val bucket_high = "bucket_high"
    val bucket_low = "bucket_low"
  }


  object CompactionStrategies {
    val SizeTieredCompactionStrategy = "SizeTieredCompactionStrategy"
  }

  object CompressorOptions {
    val chunk_length_kb = "chunk_length_kb"
    val sstable_compression = "sstable_compression"
  }

  object Compressors {
    val DeflateCompressor = "DeflateCompressor"
  }

  object StorageMechanisms {
    val CompactStorage = "COMPACT STORAGE"
  }

}
