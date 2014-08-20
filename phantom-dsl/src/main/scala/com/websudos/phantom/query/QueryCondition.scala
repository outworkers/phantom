/*
 * Copyright 2013 websudos ltd.
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
package com.websudos.phantom.query

import com.datastax.driver.core.querybuilder.{ Assignment, Clause, Ordering }

/**
 * This is a wrapper clause for primary conditions.
 * They wrap the Clause used in a "WHERE" or "AND" query.
 *
 * Only indexed columns can produce a QueryCondition via "WHERE" and "AND" operators.
 * @param clause The clause to use.
 */
case class QueryCondition(clause: Clause)

/**
 * This is wrapper clause for non-primary conditionals, the "onlyIf" part of CQL query.
 * It's used to enable queries such as "Records.update.where(_.bla eqs bla).onlyIf(_.someRecord eqs "something")
 *
 * Even if they both wrap a Clause, QueryCondition and SecondaryCondition are not interchangeable.
 * This is because using an indexed column in an "onlyIf" query throws an error, and this we can prevent it.
 * @param clause The clause to use.
 */
case class SecondaryQueryCondition(clause: Clause)

case class QueryAssignment(assignment: Assignment)

case class QueryOrdering(ordering: Ordering)


