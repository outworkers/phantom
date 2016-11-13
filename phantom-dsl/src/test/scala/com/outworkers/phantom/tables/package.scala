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
package com.outworkers.phantom

import java.net.InetAddress
import java.util.{Date, UUID}

import com.datastax.driver.core.utils.UUIDs
import com.outworkers.util.testing._
import org.joda.time.{DateTime, DateTimeZone}

package object tables {

  implicit object CounterRecordSampler extends Sample[CounterRecord] {
    def sample: CounterRecord = {
      CounterRecord(
        gen[UUID],
        gen[Long]
      )
    }
  }

  implicit object JodaRowSampler extends Sample[JodaRow] {
    def sample: JodaRow = {
      JodaRow(
        gen[String],
        gen[Int],
        gen[DateTime]
      )
    }
  }


  implicit object JsonTestSampler extends Sample[JsonTest] {
    def sample: JsonTest = JsonTest(
      gen[String],
      gen[String]
    )

  }


  implicit object SecondaryIndexRecordSampler extends Sample[SecondaryIndexRecord] {
    def sample: SecondaryIndexRecord = SecondaryIndexRecord(
      gen[UUID],
      gen[UUID],
      gen[String]
    )
  }


  implicit object JsonClassSampler extends Sample[JsonClass] {
    def sample: JsonClass = JsonClass(
      gen[UUID],
      gen[String],
      gen[JsonTest],
      genList[JsonTest](),
      genList[JsonTest]().toSet
    )
  }

  implicit object ArticleSampler extends Sample[Article] {
    def sample: Article = Article(
      gen[String],
      gen[UUID],
      gen[Long]
    )
  }

  implicit object MyTestRowSampler extends Sample[MyTestRow] {
    def sample: MyTestRow = MyTestRow(
      gen[String],
      genOpt[Int],
      genList[String]()
    )
  }

  implicit object PrimitiveSampler extends Sample[Primitive] {
    def sample: Primitive = {
      Primitive(
        gen[String],
        gen[Long],
        boolean = false,
        gen[BigDecimal],
        gen[Double],
        gen[Float],
        InetAddress.getByName("127.0.0.1"),
        gen[Int],
        gen[Date],
        gen[UUID],
        BigInt(gen[Int])
      )
    }
  }

  implicit object OptionalPrimitiveSampler extends Sample[OptionalPrimitive] {
    def sample: OptionalPrimitive = {
      OptionalPrimitive(
        gen[String],
        genOpt[String],
        genOpt[Long],
        Some(false),
        genOpt[BigDecimal],
        genOpt[Double],
        genOpt[Float],
        Some(InetAddress.getByName("127.0.0.1")),
        genOpt[Int],
        genOpt[Date],
        genOpt[UUID],
        Some(UUIDs.timeBased()),
        genOpt[BigInt]
      )
    }
  }

  implicit object PrimitiveCassandra22Sampler extends Sample[PrimitiveCassandra22] {
    def sample: PrimitiveCassandra22 = {
      PrimitiveCassandra22(
        gen[String],
        gen[Int].toShort,
        gen[Int].toByte,
        new DateTime(DateTimeZone.UTC).plus(gen[Int].toLong).toLocalDate
      )
    }
  }

  implicit object OptionalPrimitiveCassandra22Sampler extends Sample[OptionalPrimitiveCassandra22] {
    def sample: OptionalPrimitiveCassandra22 = {
      OptionalPrimitiveCassandra22(
        gen[String],
        genOpt[Int].map(_.toShort),
        genOpt[Int].map(_.toByte),
        Some(new DateTime(DateTimeZone.UTC).plus(gen[Int].toLong).toLocalDate)
      )
    }
  }

  implicit object TimeSeriesRSampler extends Sample[TimeSeriesRecord] {
    def sample: TimeSeriesRecord = {
      TimeSeriesRecord(
        gen[UUID],
        gen[String],
        gen[DateTime]
      )
    }
  }

  implicit object SimpleMapOfStringsClassSampler extends Sample[SimpleMapOfStringsClass] {
    def sample: SimpleMapOfStringsClass = SimpleMapOfStringsClass(genMap[String, Int](5))
  }

  implicit object TimeUUIDRecordSampler extends Sample[TimeUUIDRecord] {
    override def sample: TimeUUIDRecord = {
      val id = UUIDs.timeBased()

      TimeUUIDRecord(
        user = gen[UUID],
        id = id,
        name = gen[ShortString].value,
        timestamp = new DateTime(UUIDs.unixTimestamp(id), DateTimeZone.UTC)
      )
    }
  }

  implicit object RecipeSampler extends Sample[Recipe] {
    def sample: Recipe = {
      Recipe(
        gen[String],
        genOpt[String],
        genList[String](),
        genOpt[Int],
        gen[DateTime],
        Map.empty[String, String],
        gen[UUID]
      )
    }
  }

  implicit object SampleEventSampler extends Sample[SampleEvent] {
    def sample: SampleEvent = {
      SampleEvent(
        gen[UUID],
        Map(
          gen[Long] -> gen[DateTime],
          gen[Long] -> gen[DateTime],
          gen[Long] -> gen[DateTime],
          gen[Long] -> gen[DateTime],
          gen[Long] -> gen[DateTime]
        )
      )
    }
  }

  implicit object TestRowSampler extends Sample[TestRow] {
    def sample: TestRow = TestRow(
      gen[String],
      genList[String](),
      genList[String]().toSet,
      genMap[String, String](5),
      genList[Int]().toSet,
      genMap[Int, String](5),
      genMap[Int, Int](5)
    )
  }


  implicit object TestListSampler extends Sample[TestList] {
    def sample: TestList = TestList(
      gen[String],
      genList[String]()
    )
  }

  implicit object TestRow2Sampler extends Sample[TestRow2] {
    def sample: TestRow2 = {
      TestRow2(
        gen[String],
        genOpt[Int],
        gen[SimpleMapOfStringsClass],
        genOpt[SimpleMapOfStringsClass],
        genMap[String, SimpleMapOfStringsClass](5)
      )
    }
  }

  implicit object StaticCollectionRecordSampler extends Sample[StaticCollectionRecord] {
    def sample: StaticCollectionRecord = {
      StaticCollectionRecord(
        gen[UUID],
        gen[UUID],
        genList[String]()
      )
    }
  }

  implicit object SimpleStringClassSampler extends Sample[SimpleStringClass] {
    def sample: SimpleStringClass = SimpleStringClass(gen[String])
  }

  implicit object TupleRecordSampler extends Sample[TupleRecord] {
    override def sample: TupleRecord = TupleRecord(gen[UUID], gen[String] -> gen[Long])
  }

  implicit object NestedTupleRecordSampler extends Sample[NestedTupleRecord] {
    override def sample: NestedTupleRecord = NestedTupleRecord(
      gen[UUID], gen[String] -> (gen[String] -> gen[Long])
    )
  }

  implicit object TupleCollectionRecordSampler extends Sample[TupleCollectionRecord] {
    override def sample: TupleCollectionRecord = TupleCollectionRecord(
      gen[UUID], genList[Int]().map(_ -> gen[String])
    )
  }
}
