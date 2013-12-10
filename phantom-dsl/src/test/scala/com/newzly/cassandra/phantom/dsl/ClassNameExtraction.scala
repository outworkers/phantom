package com.newzly.cassandra.phantom.dsl

import java.net.InetAddress
import java.util.{ Date, UUID }

import scala.concurrent.{ Await, Future }
import scala.concurrent.duration.Duration
import scala.concurrent.ExecutionContext.Implicits.global
import scala.reflect.ClassTag

import org.scalatest.FlatSpec

import com.datastax.driver.core.{ Session, Row }
import com.datastax.driver.core.utils.UUIDs

import com.newzly.cassandra.phantom._
import com.newzly.cassandra.phantom.CassandraTable


trait Test {
  private[this] lazy val _name: String = {
    val fullName = getClass.getName.split("\\.").toList.last
    val index = fullName.indexOf("$$anonfun")

    if (index != -1) {
    val str = fullName.substring(index + 9, fullName.length)
    str.replaceAll("[(\\$\\d+\\$)]", "")
    } else {
      fullName.replaceAll("[(\\$\\d+\\$)]", "");
    }
  }
  //^[^_]*_
  val name: String = _name
}

object Test extends PrimitiveColumn[String] {

}


  class TestNames {

    private[this] lazy val _name: String = {
      val fullName = getClass.getName.split("\\.").toList.last
      val index = fullName.indexOf("$$anonfun")
      val str = fullName.substring(index + 9, fullName.length)
      str.replaceAll("(\\$\\d+\\$)", "")
    }
    def name: String = _name
  }

  class Parent extends TestNames
  class Parent2 extends Parent

class ClassNameExtraction extends FlatSpec {

  it should "correctly extract the object name " in {
    assert(Test.name === "Test")
  }

  it should "correctly extract the table name" in {
    object TestNames extends TestNames

    assert(TestNames.name === "TestNames")
  }


  it should "correctly extract the parent name" in {
    object Parent extends Parent

    assert(Parent.name === "Parent")
  }

  it should "correctly extract the column names" in {
    object Parent2 extends Parent2
    assert(Parent2.name === "Parent2")
  }

}
