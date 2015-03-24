package com.websudos.phantom.builder

import com.websudos.phantom.builder.query.CQLQuery

sealed trait CreateOptionsBuilder {
  protected[this] def quotedValue(qb: CQLQuery, option: String, value: String): CQLQuery = {
    qb.append(CQLSyntax.comma)
      .forcePad.appendSingleQuote(option)
      .forcePad.append(CQLSyntax.Symbols.`:`)
      .forcePad.appendSingleQuote(value)
  }

  protected[this] def simpleValue(qb: CQLQuery, option: String, value: String): CQLQuery = {
    qb.append(CQLSyntax.comma)
      .forcePad.appendSingleQuote(option)
      .forcePad.append(CQLSyntax.Symbols.`:`)
      .forcePad.append(value)
  }
}

sealed trait CompactionQueryBuilder extends CreateOptionsBuilder {

  def min_sstable_size(qb: CQLQuery, size: String): CQLQuery = {
    quotedValue(qb, CQLSyntax.CompactionOptions.min_sstable_size, size)
  }

  def sstable_size_in_mb(qb: CQLQuery, size: String): CQLQuery = {
    quotedValue(qb, CQLSyntax.CompactionOptions.sstable_size_in_mb, size)
  }

  def tombstone_compaction_interval(qb: CQLQuery, size: String): CQLQuery = {
    quotedValue(qb, CQLSyntax.CompactionOptions.tombstone_compaction_interval, size)
  }

  def tombstone_threshold(qb: CQLQuery, size: Double): CQLQuery = {
    simpleValue(qb, CQLSyntax.CompactionOptions.tombstone_threshold, size.toString)
  }

  def bucket_high(qb: CQLQuery, size: Double): CQLQuery = {
    simpleValue(qb, CQLSyntax.CompactionOptions.bucket_high, size.toString)
  }

  def bucket_low(qb: CQLQuery, size: Double): CQLQuery = {
    simpleValue(qb, CQLSyntax.CompactionOptions.bucket_low, size.toString)
  }
}

sealed trait CompressionQueryBuilder extends CreateOptionsBuilder {

  def chunk_length_kb(qb: CQLQuery, size: String): CQLQuery = {
    quotedValue(qb, CQLSyntax.CompressionOptions.chunk_length_kb, size)
  }

  def crc_check_chance(qb: CQLQuery, size: Double): CQLQuery = {
    simpleValue(qb, CQLSyntax.CompressionOptions.crc_check_chance, size.toString)
  }
}


private[builder] class CreateTableBuilder extends CompactionQueryBuilder with CompressionQueryBuilder {

  private[this] def tableOption(option: String, value: String): CQLQuery = {
    Utils.concat(option, CQLSyntax.Symbols.`=`, value)
  }

  private[this] def tableOption(option: String, value: CQLQuery): CQLQuery = {
    tableOption(option, value.queryString)
  }

  def read_repair_chance(st: String): CQLQuery = {
    tableOption(CQLSyntax.CreateOptions.read_repair_chance, st)
  }

  def dclocal_read_repair_chance(st: String): CQLQuery = {
    tableOption(CQLSyntax.CreateOptions.dclocal_read_repair_chance, st)
  }

  def default_time_to_live(st: String): CQLQuery = {
    tableOption(CQLSyntax.CreateOptions.default_time_to_live, st)
  }

  def gc_grace_seconds(st: String): CQLQuery = {
    tableOption(CQLSyntax.CreateOptions.gc_grace_seconds, st)
  }

  def populate_io_cache_on_flush(st: String): CQLQuery = {
    tableOption(CQLSyntax.CreateOptions.populate_io_cache_on_flush, st)
  }

  def bloom_filter_fp_chance(st: String): CQLQuery = {
    tableOption(CQLSyntax.CreateOptions.bloom_filter_fp_chance, st)
  }

  def replicate_on_write(st: String): CQLQuery = {
    tableOption(CQLSyntax.CreateOptions.replicate_on_write, st)
  }

  def compression(qb: CQLQuery) : CQLQuery = {
    tableOption(CQLSyntax.CreateOptions.compression, qb).pad.appendIfAbsent(CQLSyntax.Symbols.`}`)
  }

  def compaction(qb: CQLQuery) : CQLQuery = {
    tableOption(CQLSyntax.CreateOptions.compaction, qb).pad.appendIfAbsent(CQLSyntax.Symbols.`}`)
  }

  def comment(qb: String): CQLQuery = {
    tableOption(CQLSyntax.CreateOptions.comment, CQLQuery.empty.appendSingleQuote(qb))
  }

  def caching(qb: String): CQLQuery = {
    tableOption(CQLSyntax.CreateOptions.caching, CQLQuery.empty.appendSingleQuote(qb))
  }

  def `with`(qb: CQLQuery, clause: CQLQuery): CQLQuery = {
    qb.pad.append(CQLSyntax.`with`).pad.append(clause)
  }

}
