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
  val `with` = "WITH"
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
  val static = "static"


  object Collections {
    val list = "list"
    val map = "map"
    val set = "set"
  }

  object Symbols {
    val `{` = "{"
    val `}` = "}"
    val `:` = ":"
    val `(` = "("
    val `)` = ")"
    val `,` = ","
    val `<` = "<"
    val `>` = ">"
    val `=` = "="
  }

  object CreateOptions {
    val comment = "comment"
    val compaction = "compaction"
    val compression = "compression"
    val read_repair_chance = "read_repair_chance"
    val dclocal_read_repair_chance = "dclocal_read_repair_chance"
    val caching = "caching"
    val replicate_on_write = "replicate_on_write"
    val gc_grace_seconds = "gc_grace_seconds"
    val populate_io_cache_on_flush = "populate_io_cache_on_flush"
    val bloom_filter_fp_chance = "bloom_filter_fp_chance"
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

  object CompressionOptions {
    val chunk_length_kb = "chunk_length_kb"
    val sstable_compression = "sstable_compression"
    val crc_check_chance = "crc_check_chance"
  }

  object CompressionStrategies {
    val DeflateCompressor = "DeflateCompressor"
    val SnappyCompressor = "SnappyCompressor"
    val LZ4Compressor = "LZ4Compressor"
  }

  object StorageMechanisms {
    val CompactStorage = "COMPACT STORAGE"
  }

}
