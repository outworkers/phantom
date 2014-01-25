package com.newzly.phantom.dsl

import java.util.{UUID, Date}
import org.scalatest.FlatSpec
import com.datastax.driver.core.Row
import com.newzly.phantom._
import com.newzly.phantom.helper.{Recipes, TableHelper, Primitives}

class CreateTableQueryString extends FlatSpec {

  it should "get the right query in primitives table" in {
    object Primitives extends Primitives {
      override def tableName = "Primitives"
    }

    assert(Primitives.tableName === "Primitives")
    val q = Primitives.create(
      _.boolean,
      _.bDecimal,
      _.double,
      _.float,
      _.inet,
      _.int,
      _.date,
      _.uuid,
      _.bi).queryString

    assert(q.stripMargin === "CREATE TABLE Primitives " +
        "( keyName int, " +
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
        "PRIMARY KEY (keyName));")
  }

  it should "work fine with List, Set, Map" in {
    val row = TableHelper.getAUniqueJsonTestRow

    object TestTable extends TestTable {
      override def tableName = "TestTable"
    }
    val q = TestTable.create(
      _.key,
      _.list,
      _.setText,
      _.mapTextToText,
      _.setInt,
      _.mapIntToText
    ).queryString

    assert( q==="CREATE TABLE TestTable " +
      "( key text, " +
      "list list<text>, " +
      "setText set<text>, " +
      "mapTextToText map<text, text>, " +
      "setInt set<int>, " +
      "mapIntToText map<int, text>, " +
      "PRIMARY KEY (key));")
  }

  it should "get the right query in mix table" in {
    object Recipes extends Recipes {
      override def tableName = "Recipes"
    }

    val q = Recipes.create(_.url,
      _.description,
      _.ingredients,
      _.author,
      _.servings,
      _.last_checked_at,
      _.props,
      _.uid).queryString

    assert(q.stripMargin === "CREATE TABLE Recipes ( "+
      "url text, " +
      "description text, " +
      "ingredients list<text>, " +
      "author text, " +
      "servings int, " +
      "last_checked_at timestamp, " +
      "props map<text, text>, " +
      "uid uuid, " +
      "PRIMARY KEY (url));")
  }
}

