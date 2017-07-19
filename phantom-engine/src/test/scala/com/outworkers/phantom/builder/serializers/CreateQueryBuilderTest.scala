/*
 * Copyright 2013 - 2017 Outworkers Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.outworkers.phantom.builder.serializers

import java.util.concurrent.TimeUnit

import com.outworkers.phantom.builder.QueryBuilder
import com.outworkers.phantom.builder.query.{OptionPart, SerializationTest}
import com.outworkers.phantom.builder.syntax.CQLSyntax
import com.outworkers.phantom.dsl._
import com.outworkers.util.samplers._
import org.joda.time.Seconds
import org.scalatest.{FreeSpec, Matchers}

import scala.concurrent.duration._

class CreateQueryBuilderTest extends FreeSpec with Matchers with SerializationTest {

  private[this] val BasicTable = db.basicTable
  final val DefaultTtl = 500
  final val OneDay = 86400

  private[this] val root = BasicTable.create.qb.queryString

  "The CREATE query builder" - {

    "should correctly serialise primary key definitions" - {
      "a simple single partition key definition" in {
        val cols = List("test")
        QueryBuilder.Create.primaryKey(cols).queryString shouldEqual "PRIMARY KEY (test)"
      }

      "a single partition key and a primary key" in {
        val partitions = List("test")
        val primaries = List("test2")
        QueryBuilder.Create.primaryKey(partitions, primaries).queryString shouldEqual "PRIMARY KEY (test, test2)"
      }

      "a composite partition key" in {
        val partitions = List("partition1", "partition2")
        val primaries = List("primary1")
        val key = QueryBuilder.Create.primaryKey(partitions, primaries).queryString

        key shouldEqual "PRIMARY KEY ((partition1, partition2), primary1)"
      }

      "a compound primary key" in {
        val partitions = List("partition1")
        val primaries = List("primary1", "primary2")
        val key = QueryBuilder.Create.primaryKey(partitions, primaries).queryString

        key shouldEqual "PRIMARY KEY (partition1, primary1, primary2)"
      }

      "a composite and compound primary key" in {
        val partitions = List("partition1", "partition2")
        val primaries = List("primary1", "primary2")
        val key = QueryBuilder.Create.primaryKey(partitions, primaries).queryString

        key shouldEqual "PRIMARY KEY ((partition1, partition2), primary1, primary2)"
      }

      "a composite with clustering order" in {
        val partitions = List("partition1", "partition2")
        val primaries = List("primary1", "primary2")
        val clustering = List("primary1 ASC", "primary2 ASC")
        val key = QueryBuilder.Create.primaryKey(partitions, primaries, clustering).queryString

        key shouldEqual "PRIMARY KEY ((partition1, partition2), primary1, primary2) WITH CLUSTERING ORDER BY (primary1 ASC, primary2 ASC)"
      }
    }

    "should create a simple percentile clause" - {
      "using the augmented number strings" in {
        val num = gen[Int]
        val qb = num.percentile.queryString
        qb shouldEqual s"$num ${CQLSyntax.CreateOptions.percentile}"
      }
    }

    "should allow using DateTieredCompactionStrategy and its options" - {
      "serialise a create query with a DateTieredCompactionStrategy" in {
        val qb = BasicTable.create.`with`(
          compaction eqs DateTieredCompactionStrategy
        ).qb.queryString

        qb shouldEqual s"$root WITH compaction = {'class': 'DateTieredCompactionStrategy'}"
      }

      "allow setting base_time_seconds" in {
        val qb = BasicTable.create.`with`(
          compaction eqs DateTieredCompactionStrategy.base_time_seconds(5L)
        ).qb.queryString

        qb shouldEqual s"$root WITH compaction = {'class': 'DateTieredCompactionStrategy', 'base_time_seconds': 5}"
      }

      "allow setting max_threshold" in {
        val qb = BasicTable.create.`with`(
          compaction eqs DateTieredCompactionStrategy.max_threshold(5)
        ).qb.queryString

        qb shouldEqual s"$root WITH compaction = {'class': 'DateTieredCompactionStrategy', 'max_threshold': 5}"
      }

      "allow setting min_threshold" in {
        val qb = BasicTable.create.`with`(
          compaction eqs DateTieredCompactionStrategy.min_threshold(5)
        ).qb.queryString

        qb shouldEqual s"$root WITH compaction = {'class': 'DateTieredCompactionStrategy', 'min_threshold': 5}"
      }

      "allow setting timestamp_resolution" in {
        val qb = BasicTable.create.`with`(
          compaction eqs DateTieredCompactionStrategy.timestamp_resolution(TimeUnit.MILLISECONDS)
        ).qb.queryString

        qb shouldEqual s"$root WITH compaction = {'class': 'DateTieredCompactionStrategy', 'timestamp_resolution': 'MILLISECONDS'}"
      }
    }

    "should allow using TimeWindowCompactionStrategy and its options" - {

      "serialise a create query with a TimeWindowCompactionStrategy" in {
        val qb = BasicTable.create.`with`(compaction eqs TimeWindowCompactionStrategy).qb.queryString

        qb shouldEqual s"$root WITH compaction = {'class': 'TimeWindowCompactionStrategy'}"
      }

      "serialise a create query with a TimeWindowCompactionStrategy and an option set" in {
        val qb = BasicTable.create.`with`(compaction eqs TimeWindowCompactionStrategy.compaction_window_unit(TimeUnit.DAYS)
        ).qb.queryString

        qb shouldEqual s"$root WITH compaction = {'class': 'TimeWindowCompactionStrategy', 'compaction_window_unit': 'DAYS'}"
      }

      "serialise a create query with a TimeWindowCompactionStrategy and two options set" in {
        val qb = BasicTable.create.`with`(compaction eqs TimeWindowCompactionStrategy
          .compaction_window_unit(TimeUnit.DAYS)
          .compaction_window_size(5)
        ).qb.queryString

        qb shouldEqual s"$root WITH compaction = {'class'" +
          ": 'TimeWindowCompactionStrategy', 'compaction_window_unit': 'DAYS', 'compaction_window_size': 5}"
      }

      "serialise a create query with a TimeWindowCompactionStrategy and three options set" in {
        val qb = BasicTable.create.`with`(compaction eqs TimeWindowCompactionStrategy
          .compaction_window_unit(TimeUnit.DAYS)
          .compaction_window_size(5)
          .timestamp_resolution(TimeUnit.MILLISECONDS)
        ).qb.queryString

        qb shouldEqual s"$root WITH compaction = {'class'" +
          ": 'TimeWindowCompactionStrategy', 'compaction_window_unit': 'DAYS', 'compaction_window_size': 5, 'timestamp_resolution': 'MILLISECONDS'}"
      }
    }

    "should allow specifying table creation options" - {

      "serialise a simple create query with a SizeTieredCompactionStrategy and no compaction strategy options set" in {

        val qb = BasicTable.create.`with`(compaction eqs SizeTieredCompactionStrategy).qb.queryString

        qb shouldEqual s"$root WITH compaction = {'class': 'SizeTieredCompactionStrategy'}"
      }

      "serialise a create query with a TimeWindowCompactionStrategy" in {
        val qb = BasicTable.create.`with`(compaction eqs TimeWindowCompactionStrategy).qb.queryString

        qb shouldEqual s"$root WITH compaction = {'class': 'TimeWindowCompactionStrategy'}"
      }

      "serialise a create query with a TimeWindowCompactionStrategy and an option set" in {
        val qb = BasicTable.create.`with`(compaction eqs TimeWindowCompactionStrategy.compaction_window_unit(TimeUnit.DAYS)
        ).qb.queryString

        qb shouldEqual s"$root WITH compaction = {'class': 'TimeWindowCompactionStrategy', 'compaction_window_unit': 'DAYS'}"
      }


      "serialise a create query with a TimeWindowCompactionStrategy and both options set" in {
        val qb = BasicTable.create.`with`(compaction eqs TimeWindowCompactionStrategy
          .compaction_window_unit(TimeUnit.DAYS)
          .compaction_window_size(5)
        ).qb.queryString

        qb shouldEqual s"$root WITH compaction = {'class': 'TimeWindowCompactionStrategy', 'compaction_window_unit': 'DAYS', 'compaction_window_size': 5}"
      }

      "serialise a simple create query with a SizeTieredCompactionStrategy and 1 compaction strategy options set" in {

        val qb = BasicTable.create.`with`(
          compaction eqs LeveledCompactionStrategy.sstable_size_in_mb(50)
        ).qb.queryString

        qb shouldEqual "CREATE TABLE phantom.basicTable (id uuid, id2 uuid, id3 uuid, placeholder text, PRIMARY KEY (id, id2, id3)) WITH compaction = {'class': 'LeveledCompactionStrategy', 'sstable_size_in_mb': 50}"
      }

      "serialise a simple create query with a SizeTieredCompactionStrategy and 1 compaction strategy options set and a compression strategy set" in {
        val qb = BasicTable.create
          .`with`(compaction eqs LeveledCompactionStrategy.sstable_size_in_mb(50))
          .and(compression eqs LZ4Compressor.crc_check_chance(0.5))
          .qb.queryString

        qb shouldEqual s"""$root WITH compaction = {'class': 'LeveledCompactionStrategy', 'sstable_size_in_mb': 50} AND compression = {'sstable_compression': 'LZ4Compressor', 'crc_check_chance': 0.5}"""
      }

      "add a comment option to a create query" in {
        val qb = BasicTable.create
          .`with`(comment eqs "testing")
          .qb.queryString

        qb shouldEqual s"$root WITH comment = 'testing'"
      }

      "allow specifying a read_repair_chance clause" in {
        val qb = BasicTable.create.`with`(read_repair_chance eqs 5D).qb.queryString
        qb shouldEqual s"$root WITH read_repair_chance = 5.0"
      }

      "allow specifying a dclocal_read_repair_chance clause" in {
        val qb = BasicTable.create.`with`(dclocal_read_repair_chance eqs 5D).qb.queryString
        qb shouldEqual s"$root WITH dclocal_read_repair_chance = 5.0"
      }

      "allow specifying a replicate_on_write clause" in {
        val qb = BasicTable.create.`with`(replicate_on_write eqs true).qb.queryString
        qb shouldEqual s"$root WITH replicate_on_write = true"
      }

      "allow specifying a custom gc_grace_seconds clause" in {
        val qb = BasicTable.create.`with`(gc_grace_seconds eqs 5.seconds).qb.queryString
        qb shouldEqual s"$root WITH gc_grace_seconds = 5"
      }

      "allow specifying larger custom units as gc_grace_seconds" in {
        val qb = BasicTable.create.`with`(gc_grace_seconds eqs 1.day).qb.queryString
        qb shouldEqual s"$root WITH gc_grace_seconds = 86400"
      }

      "allow specifying custom gc_grade_seconds using the Joda Time ReadableInstant and Second API" in {
        val qb = BasicTable.create.`with`(gc_grace_seconds eqs Seconds.seconds(OneDay)).qb.queryString
        qb shouldEqual s"$root WITH gc_grace_seconds = 86400"
      }

      "allow specifying a bloom_filter_fp_chance using a Double param value" in {
        val qb = BasicTable.create.`with`(bloom_filter_fp_chance eqs 5D).qb.queryString
        qb shouldEqual s"$root WITH bloom_filter_fp_chance = 5.0"
      }
    }

    "should allow specifying cache strategies " - {
      "specify Cache.None as a cache strategy" in {
        val qb = BasicTable.create.`with`(caching eqs Cache.None()).qb.queryString

        if (session.v4orNewer) {
          qb shouldEqual s"$root WITH caching = {'keys': 'none', 'rows_per_partition': 'none'}"
        } else {
          qb shouldEqual s"$root WITH caching = 'none'"
        }
      }

      "specify Cache.KeysOnly as a caching strategy" in {
        val qb = BasicTable.create.`with`(caching eqs Cache.KeysOnly()).qb.queryString

        if (session.v4orNewer) {
          qb shouldEqual s"$root WITH caching = {'keys': 'all', 'rows_per_partition': 'none'}"
        } else {
          qb shouldEqual s"$root WITH caching = 'keys_only'"
        }
      }

      "specify Cache.RowsOnly as a caching strategy" in {
        val qb = BasicTable.create.`with`(caching eqs Cache.RowsOnly()).qb.queryString

        if (session.v4orNewer) {
          qb shouldEqual s"$root WITH caching = {'rows_per_partition': 'all'}"
        } else {
          qb shouldEqual s"$root WITH caching = 'rows_only'"
        }
      }

      "specify a Cache rows_per_partition as an integer value" in {
        val qb = BasicTable.create.`with`(caching eqs Cache.All().rows_per_partition(5)).qb.queryString
        qb shouldEqual s"$root WITH caching = {'keys': 'all', 'rows_per_partition': 'all', 'rows_per_partition': 5}"
      }

      "specify Cache.All as a caching strategy" in {
        val qb = BasicTable.create.`with`(caching eqs Cache.All()).qb.queryString
        if (session.v4orNewer) {
          qb shouldEqual s"$root WITH caching = {'keys': 'all', 'rows_per_partition': 'all'}"
        } else {
          qb shouldEqual s"$root WITH caching = 'all'"
        }

      }
    }

    "should allow specifying a default_time_to_live" - {
      "specify a default time to live using a Long value" in {
        val qb = BasicTable.create.`with`(default_time_to_live eqs DefaultTtl.toLong).qb.queryString
        qb shouldEqual s"$root WITH default_time_to_live = 500"
      }

      "specify a default time to live using a org.joda.time.Seconds value" in {
        val qb = BasicTable.create.`with`(default_time_to_live eqs Seconds.seconds(DefaultTtl)).qb.queryString
        qb shouldEqual s"$root WITH default_time_to_live = 500"
      }

      "specify a default time to live using a scala.concurrent.duration.FiniteDuration value" in {
        val qb = BasicTable.create.`with`(
          default_time_to_live eqs FiniteDuration(DefaultTtl, TimeUnit.SECONDS)
        ).qb.queryString
        qb shouldEqual s"$root WITH default_time_to_live = 500"
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

    "should allow using SizeTieredCompaction and all its properties" - {
      "specify a SizeTieredCompactionStrategy" in {

        val qb = BasicTable.create
          .option(compaction eqs SizeTieredCompactionStrategy).qb.queryString

        qb shouldEqual s"$root WITH compaction = {'class': 'SizeTieredCompactionStrategy'}"
      }

      "specify a SizeTieredCompactionStrategy with a tombstone threshold" in {

        val qb = BasicTable.create
          .option(compaction eqs SizeTieredCompactionStrategy
            .tombstone_threshold(5D)
          ).qb.queryString

        qb shouldEqual s"$root WITH compaction = {'class': 'SizeTieredCompactionStrategy', 'tombstone_threshold': 5.0}"
      }

      "specify a SizeTieredCompactionStrategy with a tombstone compaction interval" in {

        val qb = BasicTable.create
          .option(compaction eqs SizeTieredCompactionStrategy
            .tombstone_compaction_interval(5L)
          ).qb.queryString

        qb shouldEqual s"$root WITH compaction = {'class': 'SizeTieredCompactionStrategy', 'tombstone_compaction_interval': 5}"
      }

      "specify a SizeTieredCompactionStrategy with an unchecked tombstone compaction option" in {
        val qb = BasicTable.create
          .option(compaction eqs SizeTieredCompactionStrategy
            .unchecked_tombstone_compaction(5D)
          ).qb.queryString

        qb shouldEqual s"$root WITH compaction = {'class': 'SizeTieredCompactionStrategy', 'unchecked_tombstone_compaction': 5.0}"
      }

      "specify a SizeTieredCompactionStrategy with a max_threshold option" in {
        val qb = BasicTable.create
          .option(compaction eqs SizeTieredCompactionStrategy
            .max_threshold(5)
          ).qb.queryString

        qb shouldEqual s"$root WITH compaction = {'class': 'SizeTieredCompactionStrategy', 'max_threshold': 5}"
      }

      "specify a SizeTieredCompactionStrategy with a min_threshold option" in {
        val qb = BasicTable.create
          .option(compaction eqs SizeTieredCompactionStrategy
            .min_threshold(5)
          ).qb.queryString

        qb shouldEqual s"$root WITH compaction = {'class': 'SizeTieredCompactionStrategy', 'min_threshold': 5}"
      }

      "specify a SizeTieredCompactionStrategy with a cold_reads_to_omit option" in {
        val qb = BasicTable.create
          .option(compaction eqs SizeTieredCompactionStrategy
            .cold_reads_to_omit(5D)
          ).qb.queryString

        qb shouldEqual s"$root WITH compaction = {'class': 'SizeTieredCompactionStrategy', 'cold_reads_to_omit': 5.0}"
      }

      "specify a SizeTieredCompactionStrategy with a bucket_low option" in {
        val qb = BasicTable.create
          .option(compaction eqs SizeTieredCompactionStrategy
            .bucket_low(5D)
          ).qb.queryString

        qb shouldEqual s"$root WITH compaction = {'class': 'SizeTieredCompactionStrategy', 'bucket_low': 5.0}"
      }

      "specify a SizeTieredCompactionStrategy with a bucket_high option" in {
        val qb = BasicTable.create
          .option(compaction eqs SizeTieredCompactionStrategy
            .bucket_high(5D)
          ).qb.queryString

        qb shouldEqual s"$root WITH compaction = {'class': 'SizeTieredCompactionStrategy', 'bucket_high': 5.0}"
      }

      "specify a SizeTieredCompactionStrategy with a min_sstable_size option" in {
        val qb = BasicTable.create
          .option(compaction eqs SizeTieredCompactionStrategy
            .min_sstable_size(5)
          ).qb.queryString

        qb shouldEqual s"$root WITH compaction = {'class': 'SizeTieredCompactionStrategy', 'min_sstable_size': 5}"
      }


      "specify a SizeTieredCompactionStrategy with an enabled flag set to true" in {
        val qb = BasicTable.create
          .option(compaction eqs SizeTieredCompactionStrategy
            .enabled(true)
          ).qb.queryString

        qb shouldEqual s"$root WITH compaction = {'class': 'SizeTieredCompactionStrategy', 'enabled': true}"
      }

      "specify a SizeTieredCompactionStrategy with an enabled flag set to false" in {
        val qb = BasicTable.create
          .option(compaction eqs SizeTieredCompactionStrategy
            .enabled(false)
          ).qb.queryString

        qb shouldEqual s"$root WITH compaction = {'class': 'SizeTieredCompactionStrategy', 'enabled': false}"
      }
    }

    "should allow generating secondary indexes based on trait mixins" - {
      "specify a secondary index on a non-map column" in {
        val qb = QueryBuilder.Create.index("t", "k", "col").queryString

        qb shouldEqual "CREATE INDEX IF NOT EXISTS t_col_idx ON k.t(col)"
      }

      "specify a secondary index on a map column for the keys of a map column" in {
        val qb = QueryBuilder.Create.mapIndex("t", "k", "col").queryString

        qb shouldEqual "CREATE INDEX IF NOT EXISTS t_col_idx ON k.t(keys(col))"
      }
    }
  }

  "should allow creating SASI indexes" - {
    "create a basic index definition from two strings" in {
      val qb = QueryBuilder.Create.sasiIndexName("table", "column")
      qb.queryString shouldEqual s"table_column_${CQLSyntax.SASI.suffix}"
    }

    "create a full SASI index definition" in {
      val index = QueryBuilder.Create.sasiIndexName("table", "column")
      val qb = QueryBuilder.Create.createSASIIndex(KeySpace("keyspace"), "table", index, "column", OptionPart.empty.qb)

      qb.queryString shouldEqual s"CREATE CUSTOM INDEX IF NOT EXISTS $index ON keyspace.table(column) USING 'org.apache.cassandra.index.sasi.SASIIndex' WITH {}"
    }
  }
}
