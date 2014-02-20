package com.newzly.phantom.query

import com.datastax.driver.core.querybuilder.Select
import com.newzly.phantom.CassandraTable

class CountQuery[Owner <: CassandraTable[Owner, Record], Record](table: CassandraTable[Owner, Record], val qb: Select) extends ExecutableStatement