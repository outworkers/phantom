package com.newzly.cassandra.phantom.query

import com.datastax.driver.core.querybuilder.Clause

case class QueryCondition(clause: Clause)
