package com.websudos.phantom.builder.query.prepared

import com.websudos.phantom.builder.query.QueryBuilderTest
import com.websudos.phantom.dsl._
import com.websudos.phantom.tables.BasicTable
import com.websudos.util.testing._

class PreparedUpdateQueryTest extends QueryBuilderTest {


  "serialise an allow filtering clause in the init position" in {
    val id = gen[UUID]

    val qb = BasicTable.select.pwhere(_.id eqs ?).allowFiltering().limit(5).bind(id)

    qb.parameters should have size 1
  }

}
