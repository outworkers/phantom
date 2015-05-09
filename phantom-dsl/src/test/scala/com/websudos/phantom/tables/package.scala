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
    def sample: SimpleMapOfStringsClass = SimpleMapOfStringsClass(genMap[String, Int](5))
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
      genMap[Int, String](5)
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

  implicit object SimpleStringClassSampler extends Sample[SimpleStringClass] {
    def sample: SimpleStringClass = SimpleStringClass(gen[String])
  }

}
