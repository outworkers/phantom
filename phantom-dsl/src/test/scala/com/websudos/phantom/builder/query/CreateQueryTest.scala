package com.websudos.phantom.builder.query

import com.twitter.conversions.storage._
import com.twitter.util.{Duration => TwitterDuration}
import com.websudos.phantom.dsl._
import com.websudos.phantom.tables.BasicTable
import org.joda.time.Seconds
import org.scalatest.{FreeSpec, Matchers}

import scala.concurrent.duration._

class CreateQueryTest extends FreeSpec with Matchers {


  "The create query builder" - {
    "should allow specifying table creation options" - {

      "serialise a simple create query with a SizeTieredCompactionStrategy and no compaction strategy options set" in {

        val qb = BasicTable.newCreate.`with`(compaction eqs SizeTieredCompactionStrategy).qb.queryString

        qb shouldEqual "CREATE TABLE BasicTable (id uuid, id2 uuid, id3 uuid, placeholder text, PRIMARY KEY (id, id2, id3)) WITH compaction = { 'class' " +
          ": 'SizeTieredCompactionStrategy' }"
      }

      "serialise a simple create query with a SizeTieredCompactionStrategy and 1 compaction strategy options set" in {

        val qb = BasicTable.newCreate.`with`(compaction eqs SizeTieredCompactionStrategy.sstable_size_in_mb(50.megabytes)).qb.queryString

        qb shouldEqual "CREATE TABLE BasicTable (id uuid, id2 uuid, id3 uuid, placeholder text, PRIMARY KEY (id, id2, id3)) WITH compaction = { 'class' " +
          ": 'SizeTieredCompactionStrategy', 'sstable_size_in_mb' : '50.0 MiB' }"
      }

      "serialise a simple create query with a SizeTieredCompactionStrategy and 1 compaction strategy options set and a compression strategy set" in {

        val qb = BasicTable.newCreate
          .`with`(compaction eqs SizeTieredCompactionStrategy.sstable_size_in_mb(50.megabytes))
          .and(compression eqs LZ4Compressor.crc_check_chance(0.5))
          .qb.queryString

        qb shouldEqual """CREATE TABLE BasicTable (id uuid, id2 uuid, id3 uuid, placeholder text, PRIMARY KEY (id, id2, id3)) WITH compaction = { 'class' : 'SizeTieredCompactionStrategy', 'sstable_size_in_mb' : '50.0 MiB' } AND compression = { 'sstable_compression' : 'LZ4Compressor', 'crc_check_chance' : 0.5 }"""
      }

      "add a comment option to a create query" in {
        val qb = BasicTable.newCreate
          .`with`(comment eqs "testing")
          .qb.queryString

        qb shouldEqual "CREATE TABLE BasicTable (id uuid, id2 uuid, id3 uuid, placeholder text, PRIMARY KEY (id, id2, id3)) WITH comment = 'testing'"
      }

      "allow specifying custom gc_grace_seconds" in {
        val qb = BasicTable.newCreate.`with`(gc_grace_seconds eqs 5.seconds).qb.queryString
        qb shouldEqual "CREATE TABLE BasicTable (id uuid, id2 uuid, id3 uuid, placeholder text, PRIMARY KEY (id, id2, id3)) WITH gc_grace_seconds = 5"
      }

      "allow specifying larger custom units as gc_grace_seconds" in {
        val qb = BasicTable.newCreate.`with`(gc_grace_seconds eqs 1.day).qb.queryString
        qb shouldEqual "CREATE TABLE BasicTable (id uuid, id2 uuid, id3 uuid, placeholder text, PRIMARY KEY (id, id2, id3)) WITH gc_grace_seconds = 86400"
      }

      "allow specifying larger custom units as gc_grace_seconds using the Twitter conversions API" in {
        val qb = BasicTable.newCreate.`with`(gc_grace_seconds eqs TwitterDuration.fromSeconds(86400)).qb.queryString
        qb shouldEqual "CREATE TABLE BasicTable (id uuid, id2 uuid, id3 uuid, placeholder text, PRIMARY KEY (id, id2, id3)) WITH gc_grace_seconds = 86400"
      }

      "allow specifying custom gc_grade_seconds using the Joda Time ReadableInstant and Second API" in {
        val qb = BasicTable.newCreate.`with`(gc_grace_seconds eqs Seconds.seconds(86400)).qb.queryString
        qb shouldEqual "CREATE TABLE BasicTable (id uuid, id2 uuid, id3 uuid, placeholder text, PRIMARY KEY (id, id2, id3)) WITH gc_grace_seconds = 86400"
      }

      "allow specifying a bloom_filter_fp_chance using a Double param value" in {
        val qb = BasicTable.newCreate.`with`(bloom_filter_fp_chance eqs 5D).qb.queryString
        qb shouldEqual "CREATE TABLE BasicTable (id uuid, id2 uuid, id3 uuid, placeholder text, PRIMARY KEY (id, id2, id3)) WITH bloom_filter_fp_chance = 5"
      }
    }

    "should allow specifying cache strategies " - {
      "specify Cache.None as a cache strategy" in {
        val qb = BasicTable.newCreate.`with`(caching eqs Cache.None).qb.queryString
        qb shouldEqual "CREATE TABLE BasicTable (id uuid, id2 uuid, id3 uuid, placeholder text, PRIMARY KEY (id, id2, id3)) WITH caching = "
      }
    }
  }




}
