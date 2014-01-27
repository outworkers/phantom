package com.newzly.phantom.dsl

import org.scalatest.FlatSpec
import com.newzly.phantom.tables.{ Primitives, Recipes, TestTable }

class CreateTableQueryString extends FlatSpec {

  it should "get the right query in primitives table" in {
    assert(Primitives.tableName === "Primitives")
    val q = Primitives.create.schema().queryString

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
  }

  it should "get the correct count the primitives table" in {
    assert(Primitives.columns.length === 11)
  }

  it should "work fine with List, Set, Map" in {
    val manual = s"CREATE TABLE ${TestTable.tableName} " +
      "( key text, " +
      "list list<text>, " +
      "setText set<text>, " +
      "mapTextToText map<text, text>, " +
      "setInt set<int>, " +
      "mapIntToText map<int, text>, " +
      "PRIMARY KEY (key));"

    assert(TestTable.columns.forall(column => { manual.contains(column.name) }))
  }

  it should "get the right query in mix table" in {
    val q = Recipes.create.schema().queryString

    val manual = s"CREATE TABLE ${Recipes.tableName}} ( "+
      "url text, " +
      "description text, " +
      "ingredients list<text>, " +
      "author text, " +
      "servings int, " +
      "last_checked_at timestamp, " +
      "props map<text, text>, " +
      "uid uuid, " +
      "PRIMARY KEY (url));"

      assert(Recipes.columns.forall(column => {
        manual.contains(column.name)
      }))
  }

  it should "correctly add clustering order to a query" in {
    val q = Recipes.create.schema()
      .withClusteringOrder(_.last_checked_at)
      .ascending.queryString
    assert(q.contains("ASCENDING"))
  }
}

