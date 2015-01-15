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


