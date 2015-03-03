package com.websudos.phantom.builder.query

import org.scalatest.{FlatSpec, Matchers}
import com.websudos.phantom.dsl._
import com.websudos.phantom.tables.BasicTable
import com.twitter.conversions.storage._

class CreateQueryTest extends FlatSpec with Matchers with WithClauses {



  it should "serialise a simple create query with a SizeTieredCompactionStrategy and no compaction strategy options set" in {

    val qb = BasicTable.newCreate.`with`(compaction eqs SizeTieredCompactionStrategy).qb.queryString

     qb shouldEqual "CREATE TABLE BasicTable (id uuid, id2 uuid, id3 uuid, placeholder text, PRIMARY KEY (id, id2, id3)) WITH compaction = { 'class' " +
      ": 'SizeTieredCompactionStrategy' }"
  }


  it should "serialise a simple create query with a SizeTieredCompactionStrategy and 1 compaction strategy options set" in {

    val qb = BasicTable.newCreate.`with`(compaction eqs SizeTieredCompactionStrategy.sstable_size_in_mb(50.megabytes)).qb.queryString
    Console.println(qb)

    qb shouldEqual "CREATE TABLE BasicTable (id uuid, id2 uuid, id3 uuid, placeholder text, PRIMARY KEY (id, id2, id3)) WITH compaction = { 'class' " +
      ": 'SizeTieredCompactionStrategy', 'sstable_size_in_mb' : '50.0 MiB' }"
  }

  it should "serialise a simple create query with a SizeTieredCompactionStrategy and 1 compaction strategy options set and a compression strategy set" in {

    val qb = BasicTable.newCreate
      .`with`(compaction eqs SizeTieredCompactionStrategy.sstable_size_in_mb(50.megabytes))
      .and(compression eqs LZ4Compressor.crc_check_chance(0.5))
      .qb.queryString

    qb shouldEqual """CREATE TABLE BasicTable (id uuid, id2 uuid, id3 uuid, placeholder text, PRIMARY KEY (id, id2, id3)) WITH compaction = { 'class' : 'SizeTieredCompactionStrategy', 'sstable_size_in_mb' : '50.0 MiB' } AND compression = { 'sstable_compression' : 'LZ4Compressor', 'crc_check_chance' : 0.5 }"""
  }
}
