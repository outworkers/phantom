package com.newzly.phantom.dsl

import org.scalatest.FlatSpec
import com.newzly.phantom.tables.{ Primitives, Recipes, TestTable }

class CreateTableQueryString extends FlatSpec {

  it should "get the right query in primitives table" in {
    assert(Primitives.tableName === "Primitives")
    val q = Primitives.createSchema

    Console.println(q)

    val manual = "CREATE TABLE Primitives " +
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
  }

  it should "get the correct count the primitives table" in {
    assert(Primitives.columns.length === 11)
  }

  it should "work fine with List, Set, Map" in {
    val q = TestTable.createSchema

    assert(q.indexOf("list list<text>") > 0)
    assert(q.indexOf("setText set<text>") > 0 )
    assert(q.indexOf("mapIntToText map<int, text>") > 0)
    assert(q.indexOf("setInt set<int>") > 0)
    assert(q.indexOf("key text") > 0)
    assert(q.indexOf("mapTextToText map<text, text>") > 0)
    assert(q.indexOf("PRIMARY KEY (key)") > 0 )

    assert( q.replace("CREATE TABLE TestTable ( ","" )
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

  it should "get the right query in mix table" in {
    val q = Recipes.createSchema
    Console.println(q)
    assert(q.indexOf("url text") > 0)
    assert(q.indexOf("description text") > 0)
    assert(q.indexOf("ingredients list<text>") > 0)
    assert(q.indexOf("author text") > 0)
    assert(q.indexOf("servings int") > 0)
    assert(q.indexOf("last_checked_at timestamp") > 0)
    assert(q.indexOf("props map<text, text>") > 0)
    assert(q.indexOf("uid uuid") > 0)
    assert(q.indexOf("PRIMARY KEY (url)") > 0)

    assert( q.replace("CREATE TABLE Recipes ( ","" )
      .replace("url text","")
      .replace("description text","")
      .replace("ingredients list<text>","")
      .replace("author text","")
      .replace("servings int","")
      .replace("last_checked_at timestamp","")
      .replace("props map<text, text>","")
      .replace("uid uuid","")
      .replace("PRIMARY KEY (url)","")
      .replace(")","")
      .replace(" ","")
      .replace(",","") == ";" )

  }

  ignore should "correctly add clustering order to a query" in {
    /*val q = Recipes.create()
      .withClusteringOrder(_.last_checked_at)
      .ascending.queryString
    Console.println(q)*/
  }
}

