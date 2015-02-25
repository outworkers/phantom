package com.websudos.phantom.builder.query

import com.websudos.phantom.tables.BasicTable
import org.scalatest.{FlatSpec, Matchers}

class CreateQueryTest extends FlatSpec with Matchers with WithClauses {



  it should "serialise a simple create query" in {
    Console.println(BasicTable.newCreate.`with`(Compaction.SizeTieredCompactionStrategy).qb.queryString)
  }


}
