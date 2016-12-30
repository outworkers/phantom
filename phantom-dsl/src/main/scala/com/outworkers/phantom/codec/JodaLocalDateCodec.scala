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
package com.outworkers.phantom.codec

import com.datastax.driver.core._
import com.datastax.driver.extras.codecs.MappingCodec

class JodaLocalDateCodec extends MappingCodec(TypeCodec.date(), classOf[org.joda.time.LocalDate]) {

  override def serialize(value: org.joda.time.LocalDate): LocalDate = {
    LocalDate.fromYearMonthDay(
      value.getYear,
      value.getMonthOfYear,
      value.getDayOfMonth
    )
  }

  override def deserialize(value: LocalDate): org.joda.time.LocalDate = {
    new org.joda.time.LocalDate(
      value.getYear,
      value.getMonth,
      value.getDay
    )
  }
}