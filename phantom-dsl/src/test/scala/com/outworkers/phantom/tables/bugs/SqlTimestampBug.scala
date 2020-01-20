/*
 * Copyright 2013 - 2020 Outworkers Ltd.
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
package com.outworkers.phantom.tables.bugs

import java.sql.Timestamp

import com.outworkers.phantom.dsl._

case class CompanyWideGenderDistributionYearly(
  enterprise_id: String,
  year: Timestamp,
  hired_female_count: BigInt,
  terminated_female_count: BigInt,
  hired_male_count: BigInt,
  terminated_male_count: BigInt,
  total_female_count: BigInt,
  total_male_count: BigInt
)

abstract class YearlyGenderDistributionTable extends Table[YearlyGenderDistributionTable, CompanyWideGenderDistributionYearly] {
  object enterprise_id           extends StringColumn with PartitionKey
  object year                    extends DateTimeColumn with ClusteringOrder with Descending
  object hired_female_count      extends BigIntColumn
  object terminated_female_count extends BigIntColumn
  object hired_male_count        extends BigIntColumn
  object terminated_male_count   extends BigIntColumn
  object total_female_count      extends BigIntColumn
  object total_male_count        extends BigIntColumn
}