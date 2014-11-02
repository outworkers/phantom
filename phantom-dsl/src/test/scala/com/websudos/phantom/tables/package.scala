/*
 *
 *  * Copyright 2014 websudos ltd.
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *     http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */

package com.websudos.phantom

import java.net.InetAddress
import java.util.{Date, UUID}

import org.joda.time.DateTime
import com.websudos.util.testing._

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
      val d = new DateTime()
      JodaRow(
        gen[String],
        gen[Int],
        new DateTime(d.plus(gen[Int].toLong))
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
        genOpt[BigInt]
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
    def sample: SimpleMapOfStringsClass = SimpleMapOfStringsClass(genMap[Int]())
  }

  implicit object RecipeSampler extends Sample[Recipe] {
    def sample: Recipe = {
      Recipe(
        gen[String],
        genOpt[String],
        genList[String](),
        genOpt[Int],
        gen[DateTime],
        Map.empty[String, String]
      )
    }
  }

  implicit object TestRowSampler extends Sample[TestRow] {
    def sample: TestRow = TestRow(
      gen[String],
      genList[String](),
      genList[String]().toSet,
      genMap[String](),
      genList[Int]().toSet,
      genMap[Int]().map(_.swap)
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
        genMap[SimpleMapOfStringsClass]()
      )
    }
  }

  implicit object SimpleStringClassSampler extends Sample[SimpleStringClass] {
    def sample: SimpleStringClass = SimpleStringClass(gen[String])
  }

}
