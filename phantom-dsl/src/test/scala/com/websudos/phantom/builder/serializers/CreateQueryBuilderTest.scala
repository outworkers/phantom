/*
 * Copyright 2013-2015 Websudos, Limited.
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * - Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 *
 * - Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *
 * - Explicit consent must be obtained from the copyright owner, Websudos Limited before any redistribution is made.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package com.websudos.phantom.builder.serializers

import java.util.concurrent.TimeUnit

import com.twitter.conversions.storage._
import com.twitter.util.{Duration => TwitterDuration}
import com.websudos.phantom.builder.QueryBuilder
import com.websudos.phantom.builder.query.KeySpaceSuite
import com.websudos.phantom.builder.syntax.CQLSyntax
import com.websudos.phantom.dsl._
import com.websudos.phantom.tables.TestDatabase
import org.joda.time.Seconds
import org.scalatest.{FreeSpec, Matchers}

import scala.concurrent.duration._

class CreateQueryBuilderTest extends FreeSpec with Matchers with KeySpaceSuite {

  val BasicTable = TestDatabase.basicTable
  final val DefaultTtl = 500
  final val OneDay = 86400

  "The CREATE query builder" - {
    "should allow specifying table creation options" - {

      "serialise a simple create query with a SizeTieredCompactionStrategy and no compaction strategy options set" in {

        val qb = BasicTable.create.`with`(compaction eqs SizeTieredCompactionStrategy).qb.queryString

        qb shouldEqual "CREATE TABLE phantom.basicTable (id uuid, id2 uuid, id3 uuid, placeholder text, PRIMARY KEY (id, id2, id3)) WITH compaction = { 'class' " +
          ": 'SizeTieredCompactionStrategy' }"
      }

      "serialise a simple create query with a SizeTieredCompactionStrategy and 1 compaction strategy options set" in {

        val qb = BasicTable.create.`with`(compaction eqs LeveledCompactionStrategy.sstable_size_in_mb(50.megabytes)).qb.queryString

        qb shouldEqual "CREATE TABLE phantom.basicTable (id uuid, id2 uuid, id3 uuid, placeholder text, PRIMARY KEY (id, id2, id3)) WITH compaction = { 'class' " +
          ": 'LeveledCompactionStrategy', 'sstable_size_in_mb' : '50' }"
      }

      "serialise a simple create query with a SizeTieredCompactionStrategy and 1 compaction strategy options set and a compression strategy set" in {
        val qb = BasicTable.create
          .`with`(compaction eqs LeveledCompactionStrategy.sstable_size_in_mb(50.megabytes))
          .and(compression eqs LZ4Compressor.crc_check_chance(0.5))
        .qb.queryString

        qb shouldEqual """CREATE TABLE phantom.basicTable (id uuid, id2 uuid, id3 uuid, placeholder text, PRIMARY KEY (id, id2, id3)) WITH compaction = { 'class' : 'LeveledCompactionStrategy', 'sstable_size_in_mb' : '50' } AND compression = { 'sstable_compression' : 'LZ4Compressor', 'crc_check_chance' : 0.5 }"""
      }

      "add a comment option to a create query" in {
        val qb = BasicTable.create
          .`with`(comment eqs "testing")
          .qb.queryString

        qb shouldEqual "CREATE TABLE phantom.basicTable (id uuid, id2 uuid, id3 uuid, placeholder text, PRIMARY KEY (id, id2, id3)) WITH comment = 'testing'"
      }

      "allow specifying a read_repair_chance clause" in {
        val qb = BasicTable.create.`with`(read_repair_chance eqs 5D).qb.queryString
        qb shouldEqual "CREATE TABLE phantom.basicTable (id uuid, id2 uuid, id3 uuid, placeholder text, PRIMARY KEY (id, id2, id3)) WITH read_repair_chance = 5.0"
      }

      "allow specifying a dclocal_read_repair_chance clause" in {
        val qb = BasicTable.create.`with`(dclocal_read_repair_chance eqs 5D).qb.queryString
        qb shouldEqual "CREATE TABLE phantom.basicTable (id uuid, id2 uuid, id3 uuid, placeholder text, PRIMARY KEY (id, id2, id3)) WITH dclocal_read_repair_chance = 5.0"
      }

      "allow specifying a replicate_on_write clause" in {
        val qb = BasicTable.create.`with`(replicate_on_write eqs true).qb.queryString
        qb shouldEqual "CREATE TABLE phantom.basicTable (id uuid, id2 uuid, id3 uuid, placeholder text, PRIMARY KEY (id, id2, id3)) WITH replicate_on_write = true"
      }

      "allow specifying a custom gc_grace_seconds clause" in {
        val qb = BasicTable.create.`with`(gc_grace_seconds eqs 5.seconds).qb.queryString
        qb shouldEqual "CREATE TABLE phantom.basicTable (id uuid, id2 uuid, id3 uuid, placeholder text, PRIMARY KEY (id, id2, id3)) WITH gc_grace_seconds = 5"
      }

      "allow specifying larger custom units as gc_grace_seconds" in {
        val qb = BasicTable.create.`with`(gc_grace_seconds eqs 1.day).qb.queryString
        qb shouldEqual "CREATE TABLE phantom.basicTable (id uuid, id2 uuid, id3 uuid, placeholder text, PRIMARY KEY (id, id2, id3)) WITH gc_grace_seconds = 86400"
      }

      "allow specifying larger custom units as gc_grace_seconds using the Twitter conversions API" in {
        val qb = BasicTable.create.`with`(gc_grace_seconds eqs TwitterDuration.fromSeconds(OneDay)).qb.queryString
        qb shouldEqual "CREATE TABLE phantom.basicTable (id uuid, id2 uuid, id3 uuid, placeholder text, PRIMARY KEY (id, id2, id3)) WITH gc_grace_seconds = 86400"
      }

      "allow specifying custom gc_grade_seconds using the Joda Time ReadableInstant and Second API" in {
        val qb = BasicTable.create.`with`(gc_grace_seconds eqs Seconds.seconds(OneDay)).qb.queryString
        qb shouldEqual "CREATE TABLE phantom.basicTable (id uuid, id2 uuid, id3 uuid, placeholder text, PRIMARY KEY (id, id2, id3)) WITH gc_grace_seconds = 86400"
      }

      "allow specifying a bloom_filter_fp_chance using a Double param value" in {
        val qb = BasicTable.create.`with`(bloom_filter_fp_chance eqs 5D).qb.queryString
        qb shouldEqual "CREATE TABLE phantom.basicTable (id uuid, id2 uuid, id3 uuid, placeholder text, PRIMARY KEY (id, id2, id3)) WITH " +
          "bloom_filter_fp_chance = 5.0"
      }
    }

    "should allow specifying cache strategies " - {
      "specify Cache.None as a cache strategy" in {
        val qb = BasicTable.create.`with`(caching eqs Cache.None).qb.queryString
        qb shouldEqual "CREATE TABLE phantom.basicTable (id uuid, id2 uuid, id3 uuid, placeholder text, PRIMARY KEY (id, id2, id3)) WITH caching = 'none'"
      }

      "specify Cache.KeysOnly as a caching strategy" in {
        val qb = BasicTable.create.`with`(caching eqs Cache.KeysOnly).qb.queryString
        qb shouldEqual "CREATE TABLE phantom.basicTable (id uuid, id2 uuid, id3 uuid, placeholder text, PRIMARY KEY (id, id2, id3)) WITH caching = 'keys_only'"
      }

      "specify Cache.All as a caching strategy" in {
        val qb = BasicTable.create.`with`(caching eqs Cache.All).qb.queryString
        qb shouldEqual "CREATE TABLE phantom.basicTable (id uuid, id2 uuid, id3 uuid, placeholder text, PRIMARY KEY (id, id2, id3)) WITH caching = 'all'"
      }
    }

    "should allow specifying a default_time_to_live" - {
      "specify a default time to live using a Long value" in {
        val qb = BasicTable.create.`with`(default_time_to_live eqs DefaultTtl.toLong).qb.queryString
        qb shouldEqual "CREATE TABLE phantom.basicTable (id uuid, id2 uuid, id3 uuid, placeholder text, PRIMARY KEY (id, id2, id3)) WITH default_time_to_live = 500"
      }

      "specify a default time to live using a org.joda.time.Seconds value" in {
        val qb = BasicTable.create.`with`(default_time_to_live eqs Seconds.seconds(DefaultTtl)).qb.queryString
        qb shouldEqual "CREATE TABLE phantom.basicTable (id uuid, id2 uuid, id3 uuid, placeholder text, PRIMARY KEY (id, id2, id3)) WITH default_time_to_live = 500"
      }

      "specify a default time to live using a scala.concurrent.duration.FiniteDuration value" in {
        val qb = BasicTable.create.`with`(default_time_to_live eqs FiniteDuration(DefaultTtl, TimeUnit.SECONDS)).qb.queryString
        qb shouldEqual "CREATE TABLE phantom.basicTable (id uuid, id2 uuid, id3 uuid, placeholder text, PRIMARY KEY (id, id2, id3)) WITH default_time_to_live = 500"
      }

      "specify a default time to live using a com.twitter.util.Duration value" in {
        val qb = BasicTable.create.`with`(default_time_to_live eqs com.twitter.util.Duration.fromSeconds(DefaultTtl)).qb.queryString
        qb shouldEqual "CREATE TABLE phantom.basicTable (id uuid, id2 uuid, id3 uuid, placeholder text, PRIMARY KEY (id, id2, id3)) WITH default_time_to_live = 500"
      }
    }

    "should allow specifying a clustering order" - {
      "specify a single column clustering order with ascending ordering" in {
        val column = ("test", CQLSyntax.Ordering.asc) :: Nil

        val qb = QueryBuilder.Create.clusteringOrder(column).queryString

        qb shouldEqual "CLUSTERING ORDER BY (test ASC)"
      }

      "specify a single column clustering order with descending ordering" in {
        val column = ("test", CQLSyntax.Ordering.desc) :: Nil

        val qb = QueryBuilder.Create.clusteringOrder(column).queryString

        qb shouldEqual "CLUSTERING ORDER BY (test DESC)"
      }

      "specify multiple columns and preserve ordering" in {
        val column1 = ("test", CQLSyntax.Ordering.asc)
        val column2 = ("test2", CQLSyntax.Ordering.desc)

        val columns = List(column1, column2)

        val qb = QueryBuilder.Create.clusteringOrder(columns).queryString

        qb shouldEqual "CLUSTERING ORDER BY (test ASC, test2 DESC)"
      }
    }

    "should allow generating secondary indexes based on trait mixins" - {
      "specify a secondary index on a non-map column" in {
        val qb = QueryBuilder.Create.index("t", "k", "col").queryString

        qb shouldEqual "CREATE INDEX IF NOT EXISTS ON k.t(col)"
      }

      "specify a secondary index on a map column for the keys of a map column" in {
        val qb = QueryBuilder.Create.mapIndex("t", "k", "col").queryString

        qb shouldEqual "CREATE INDEX IF NOT EXISTS ON k.t(KEYS(col))"
      }
    }

  }




}
