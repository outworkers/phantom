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

package com.outworkers.phantom.builder.query.execution

import com.datastax.driver.core.policies.TokenAwarePolicy
import com.datastax.driver.core.{Session, SimpleStatement, Statement}
import com.outworkers.phantom.builder.ops.TokenizerKey
import com.outworkers.phantom.builder.query.QueryOptions
import com.outworkers.phantom.builder.query.engine.CQLQuery

case class ExecutableCqlQuery(
  qb: CQLQuery,
  options: QueryOptions = QueryOptions.empty,
  tokens: List[TokenizerKey]
) {

  def statement()(implicit session: Session): Statement = {

    val policy = session.getCluster.getConfiguration.getPolicies.getLoadBalancingPolicy

    val root = new SimpleStatement(qb.terminate.queryString)

    val routed = if (policy.isInstanceOf[TokenAwarePolicy] && tokens.nonEmpty) {
      setTokens(root.setKeyspace(session.getLoggedKeyspace))
    } else {
      root
    }

    options(routed)
  }

  def setTokens(st: SimpleStatement)(implicit session: Session): SimpleStatement = {
    st.setRoutingKey(tokens.map(_.apply(session)): _*)
  }
}

object ExecutableCqlQuery {
  def empty: ExecutableCqlQuery = ExecutableCqlQuery(CQLQuery.empty, QueryOptions.empty, Nil)
}