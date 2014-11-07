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

import org.joda.time.{DateTime, LocalDate}
import com.websudos.util.testing._

package object server {

  implicit object EquityPriceSampler extends Sample[EquityPrice] {
    def sample: EquityPrice = {
      EquityPrice(
        gen[String],
        new LocalDate(),
        gen[String],
        new DateTime(),
        BigDecimal(gen[Int])
      )
    }
  }

  implicit object OptionPriceSampler extends Sample[OptionPrice] {
    def sample: OptionPrice = OptionPrice(
      gen[String],
      new LocalDate(),
      gen[String],
      new DateTime(),
      BigDecimal(gen[Int]),
      BigDecimal(gen[Int])
    )
  }
}
