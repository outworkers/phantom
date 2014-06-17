package com.websudos.phantom.dsl

import org.scalatest.{ FlatSpec, Matchers, ParallelTestExecution }
import com.websudos.phantom.tables.{
  Primitives,
  Recipes,
  TestTable,
  TwoKeys
}

class CreateTableQueryString extends FlatSpec with Matchers with ParallelTestExecution {
  it should "create the right keys" in {
    val q = TwoKeys.schema()
    assert(q.contains("PRIMARY KEY (pkey, " +
      "intColumn1, " +
      "intColumn2, " +
      "intColumn3, " +
      "intColumn4, " +
      "intColumn5, " +
      "intColumn6, " +
      "intColumn7" +
      ")"))
  }

  it should "get the right query in primitives table" in {
    val q = Primitives.schema()
    val manual = s"CREATE TABLE ${Primitives.tableName}} " +
        "( pkey int, " +
        "longName bigint, " +
        "boolean boolean, " +
        "bDecimal decimal, " +
        "double double, " +
        "float float, " +
        "inet inet, " +
        "int int, " +
        "date timestamp, " +
        "uuid uuid, " +
        "bi varint, " +
        "PRIMARY KEY (pkey));"
    assert(Primitives.columns.forall(column => { manual.contains(column.name) }))
    assert(Primitives.columns.forall(column => { q.contains(column.name) }))
  }

  it should "get the correct count the primitives table" in {
    Primitives.columns.length shouldEqual 11
  }

  it should "work fine with List, Set, Map" in {
    TestTable.schema() shouldEqual "CREATE TABLE IF NOT EXISTS TestTable ( key text, list list<text>, setText set<text>, mapTextToText map<text, text>, setInt set<int>, mapIntToText map<int, text>, PRIMARY KEY (key));"
  }

  it should "get the right query in mix table" in {
    Recipes.schema() shouldEqual "CREATE TABLE IF NOT EXISTS Recipes ( url text, description text, ingredients list<text>, servings int, last_checked_at timestamp, props map<text, text>, uid uuid, PRIMARY KEY (url));"
  }
}

