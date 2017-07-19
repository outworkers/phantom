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

import com.outworkers.phantom.builder.primitives.Primitive
import com.outworkers.phantom.builder.query.QueryBuilderTest
import com.outworkers.phantom.builder.syntax.CQLSyntax
import com.outworkers.phantom.dsl._
import com.outworkers.phantom.tables.TestDatabase
import java.nio.ByteBuffer
import org.joda.time.Seconds

import scala.concurrent.duration._

class AlterQueryBuilderTest extends QueryBuilderTest {

  private[this] val basicTable = TestDatabase.basicTable

  private[this] val ssTableSize = 50

  final val DefaultTtl = 500
  final val OneDay = 86400

  "The ALTER query builder" - {

    "should serialise ALTER .. ADD queries" - {
      "serialise an ADD query for a column without a STATIC modifier" in {
        val qb = basicTable.alter.add("test_big_decimal", CQLSyntax.Types.Decimal).queryString
        qb shouldEqual s"ALTER TABLE phantom.basicTable ADD test_big_decimal ${CQLSyntax.Types.Decimal};"
      }

      "serialise an ADD query for a column with a STATIC modifier" in {
        val qb = basicTable.alter.add("test_big_decimal", CQLSyntax.Types.Decimal, static = true).queryString
        qb shouldEqual s"ALTER TABLE phantom.basicTable ADD test_big_decimal ${CQLSyntax.Types.Decimal} STATIC;"
      }

      "serialise an ADD query for a column without a STATIC modifier from a CQLQuery" in {
        val qb = basicTable.alter.add(basicTable.placeholder.qb).queryString
        qb shouldEqual s"ALTER TABLE phantom.basicTable ADD placeholder ${CQLSyntax.Types.Text};"
      }

      "serialise an ADD query for a column with a STATIC modifier from a CQLQuery" in {
        val qb = basicTable.alter.add(TestDatabase.staticTable.staticTest.qb).queryString
        qb shouldEqual s"ALTER TABLE phantom.basicTable ADD staticTest ${CQLSyntax.Types.Text} STATIC;"
      }
    }

    "should serialise ALTER .. DROP queries" - {
      "should serialise a DROP query based based on a column select" in {
        val qb = basicTable.alter.drop(_.placeholder).queryString
        qb shouldEqual "ALTER TABLE phantom.basicTable DROP placeholder;"
      }

      "should serialise a DROP query with no arguments to DROP a table" in {
        val qb = basicTable.alter().drop().queryString

        qb shouldEqual "DROP TABLE phantom.basicTable;"
      }

      "should serialise a DROP query based on string value" in {
        val qb = basicTable.alter.drop("test").queryString
        qb shouldEqual "ALTER TABLE phantom.basicTable DROP test;"
      }

      "should not compile DROP queries on INDEX fields" in {
        """
          | basicTable.alter.drop(_.id).queryString
        """ shouldNot compile
      }
    }

    "should serialise ALTER .. WITH queries" - {


      "serialise a simple create query with a SizeTieredCompactionStrategy and no compaction strategy options set" in {

        val qb = basicTable.alter.option(compaction eqs SizeTieredCompactionStrategy).qb.queryString

        qb shouldEqual "ALTER TABLE phantom.basicTable WITH compaction = {'class': 'SizeTieredCompactionStrategy'}"
      }

      "serialise a simple create query with a SizeTieredCompactionStrategy and 1 compaction strategy options set" in {

        val qb = basicTable.alter.option(compaction eqs LeveledCompactionStrategy.sstable_size_in_mb(ssTableSize)).qb.queryString

        qb shouldEqual s"ALTER TABLE phantom.basicTable WITH compaction = {'class': 'LeveledCompactionStrategy', 'sstable_size_in_mb': $ssTableSize}"
      }

      "serialise a simple create query with a SizeTieredCompactionStrategy and 1 compaction strategy options set and a compression strategy set" in {

        val qb = basicTable.alter
          .option(compaction eqs LeveledCompactionStrategy.sstable_size_in_mb(ssTableSize))
          .and(compression eqs LZ4Compressor.crc_check_chance(0.5))
          .qb.queryString

        qb shouldEqual s"""ALTER TABLE phantom.basicTable WITH compaction = {'class': 'LeveledCompactionStrategy', 'sstable_size_in_mb': $ssTableSize} AND compression = {'sstable_compression': 'LZ4Compressor', 'crc_check_chance': 0.5}"""
      }

      "add a comment option to a create query" in {
        val qb = basicTable.alter
          .option(comment eqs "testing")
          .qb.queryString

        qb shouldEqual "ALTER TABLE phantom.basicTable WITH comment = 'testing'"
      }

      "allow specifying a read_repair_chance clause" in {
        val qb = basicTable.alter.option(read_repair_chance eqs 5D).qb.queryString
        qb shouldEqual "ALTER TABLE phantom.basicTable WITH read_repair_chance = 5.0"
      }

      "allow specifying a dclocal_read_repair_chance clause" in {
        val qb = basicTable.alter.option(dclocal_read_repair_chance eqs 5D).qb.queryString
        qb shouldEqual "ALTER TABLE phantom.basicTable WITH dclocal_read_repair_chance = 5.0"
      }

      "allow specifying a replicate_on_write clause" in {
        val qb = basicTable.alter.option(replicate_on_write eqs true).qb.queryString
        qb shouldEqual "ALTER TABLE phantom.basicTable WITH replicate_on_write = true"
      }

      "allow specifying a custom gc_grace_seconds clause" in {
        val qb = basicTable.alter.option(gc_grace_seconds eqs 5.seconds).qb.queryString
        qb shouldEqual "ALTER TABLE phantom.basicTable WITH gc_grace_seconds = 5"
      }

      "allow specifying larger custom units as gc_grace_seconds" in {
        val qb = basicTable.alter.option(gc_grace_seconds eqs 1.day).qb.queryString
        qb shouldEqual "ALTER TABLE phantom.basicTable WITH gc_grace_seconds = 86400"
      }

      "allow specifying custom gc_grade_seconds using the Joda Time ReadableInstant and Second API" in {
        val qb = basicTable.alter.option(gc_grace_seconds eqs Seconds.seconds(OneDay)).qb.queryString
        qb shouldEqual "ALTER TABLE phantom.basicTable WITH gc_grace_seconds = 86400"
      }

      "allow specifying a bloom_filter_fp_chance using a Double param value" in {
        val qb = basicTable.alter.option(bloom_filter_fp_chance eqs 5D).qb.queryString
        qb shouldEqual "ALTER TABLE phantom.basicTable WITH " +
          "bloom_filter_fp_chance = 5.0"
      }
    }

    "should allow specifying cache strategies " - {
      "specify Cache.None as a cache strategy" in {
        val qb = basicTable.alter.option(caching eqs Cache.None()).qb.queryString

        if (session.v4orNewer) {
          qb shouldEqual "ALTER TABLE phantom.basicTable WITH caching = {'keys': 'none', 'rows_per_partition': 'none'}"
        } else {
          qb shouldEqual "ALTER TABLE phantom.basicTable WITH caching = 'none'"
        }
      }

      "specify Cache.KeysOnly as a caching strategy" in {
        val qb = basicTable.alter.option(caching eqs Cache.KeysOnly()).qb.queryString

        if (session.v4orNewer) {
          qb shouldEqual "ALTER TABLE phantom.basicTable WITH caching = {'keys': 'all', 'rows_per_partition': 'none'}"
        } else {
          qb shouldEqual "ALTER TABLE phantom.basicTable WITH caching = 'keys_only'"
        }
      }
    }

    "should allow specifying a default_time_to_live" - {
      "specify a default time to live using a Long value" in {
        val qb = basicTable.alter.option(default_time_to_live eqs DefaultTtl).qb.queryString
        qb shouldEqual "ALTER TABLE phantom.basicTable WITH default_time_to_live = 500"
      }

      "specify a default time to live using a Long value and a with clause" in {
        val qb = basicTable.alter.`with`(default_time_to_live eqs DefaultTtl).qb.queryString
        qb shouldEqual "ALTER TABLE phantom.basicTable WITH default_time_to_live = 500"
      }


      "specify a default time to live using a org.joda.time.Seconds value" in {
        val qb = basicTable.alter.option(default_time_to_live eqs Seconds.seconds(DefaultTtl)).qb.queryString
        qb shouldEqual "ALTER TABLE phantom.basicTable WITH default_time_to_live = 500"
      }

      "specify a default time to live using a org.joda.time.Seconds value and a `with` clause" in {
        val qb = basicTable.alter.`with`(default_time_to_live eqs Seconds.seconds(DefaultTtl)).qb.queryString
        qb shouldEqual "ALTER TABLE phantom.basicTable WITH default_time_to_live = 500"
      }

      "specify a default time to live using a scala.concurrent.duration.FiniteDuration value" in {
        val qb = basicTable.alter.option(default_time_to_live eqs FiniteDuration(DefaultTtl, TimeUnit.SECONDS)).qb.queryString
        qb shouldEqual "ALTER TABLE phantom.basicTable WITH default_time_to_live = 500"
      }
    }

    "should allow altering the type of a column" - {

      "alter column type from text to blob" in {
        val qb = basicTable.alter(_.placeholder)(Primitive[ByteBuffer]).queryString
        qb shouldEqual s"ALTER TABLE phantom.basicTable ALTER placeholder TYPE ${Primitive[ByteBuffer].dataType};"
      }

      "alter a column type from placedholder to test" in {

        val qb = basicTable.alter(_.placeholder, "test").queryString
        qb shouldEqual "ALTER TABLE phantom.basicTable RENAME placeholder TO test;"
      }

    }
  }
}
