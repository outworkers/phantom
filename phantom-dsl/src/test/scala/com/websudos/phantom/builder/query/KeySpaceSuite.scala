package com.websudos.phantom.builder.query

import com.websudos.phantom.dsl._
import org.scalatest.{Matchers, FreeSpec, Suite}

trait KeySpaceSuite {

  self: Suite =>

  implicit val keySpace = KeySpace("phantom")
}

trait QueryBuilderTest extends FreeSpec with Matchers with KeySpaceSuite