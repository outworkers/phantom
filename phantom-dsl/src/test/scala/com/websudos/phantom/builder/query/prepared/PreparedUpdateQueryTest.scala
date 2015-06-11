package com.websudos.phantom.builder.query.prepared

import com.websudos.phantom.builder.query.QueryBuilderTest
import com.websudos.phantom.tables.BasicTable

class PreparedUpdateQueryTest extends QueryBuilderTest {

  BasicTable.prepare().update().where(_.id eqs ?).and(_.id2 eqs ?)

}
