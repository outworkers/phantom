package com.newzly.phantom.dsl

import org.scalatest.FlatSpec
import com.newzly.phantom.tables._

class CreateTableQueryString extends FlatSpec {

  ignore should "create the right keys" in {
    val q = TwoKeys.schema()

    Console.println(TwoKeys.columns.map(_.name).mkString(" "))

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

  ignore should "get the right query in primitives table" in {
    assert(Primitives.tableName === "Primitives")
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
    assert(Primitives.columns.length === 11)
  }

  ignore should "work fine with List, Set, Map" in {
    val q = TestTable.schema()

    assert(q.indexOf("list list<text>") > 0)
    assert(q.indexOf("setText set<text>") > 0 )
    assert(q.indexOf("mapIntToText map<int, text>") > 0)
    assert(q.indexOf("setInt set<int>") > 0)
    assert(q.indexOf("key text") > 0)
    assert(q.indexOf("mapTextToText map<text, text>") > 0)
    assert(q.indexOf("PRIMARY KEY (key)") > 0 )

    assert( q.replace(s"CREATE TABLE ${TestTable.tableName} ( ","" )
      .replace("list list<text>","")
      .replace("setText set<text>","")
      .replace("mapIntToText map<int, text>","")
      .replace("setInt set<int>","")
      .replace("key text","")
      .replace("mapTextToText map<text, text>","")
      .replace("PRIMARY KEY (key)","")
      .replace(")","")
      .replace(" ","")
      .replace(",","") == ";" )

  }

  ignore should "get the right query in mix table" in {
    val q = Recipes.schema()
    assert(q.indexOf("url text") > 0)
    assert(q.indexOf("description text") > 0)
    assert(q.indexOf("ingredients list<text>") > 0)
    assert(q.indexOf("servings int") > 0)
    assert(q.indexOf("last_checked_at timestamp") > 0)
    assert(q.indexOf("props map<text, text>") > 0)
    assert(q.indexOf("uid uuid") > 0)
    assert(q.indexOf("PRIMARY KEY (url)") > 0)
    assert( q.replace(s"CREATE TABLE ${Recipes.tableName} ( ","" )
      .replace("url text","")
      .replace("description text","")
      .replace("ingredients list<text>","")
      .replace("servings int","")
      .replace("last_checked_at timestamp","")
      .replace("props map<text, text>","")
      .replace("uid uuid","")
      .replace("PRIMARY KEY (url)","")
      .replace(")","")
      .replace(" ","")
      .replace(",","") == ";" )
  }
}

