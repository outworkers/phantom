package com.websudos.phantom.builder.query

import org.scalatest.{FlatSpec, Matchers}
import com.websudos.phantom.dsl._
import com.websudos.phantom.tables.BasicTable

class CreateQueryTest extends FlatSpec with Matchers with WithClauses {



  it should "serialise a simple create query" in {
    Console.println(BasicTable.newCreate.`with`(Compaction.SizeTieredCompactionStrategy).qb.queryString)
  }


}
