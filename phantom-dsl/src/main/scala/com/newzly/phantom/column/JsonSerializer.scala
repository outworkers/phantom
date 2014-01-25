package com.newzly.phantom.column

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.experimental.ScalaObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.fasterxml.jackson.core.`type`.TypeReference

object JsonSerializer {
  val mapper = new ObjectMapper() with ScalaObjectMapper
  mapper.registerModule(DefaultScalaModule)


  def deserializeJson[T: Manifest](value: String): T = mapper.readValue(value, typeReference[T](implicitly[Manifest[T]]))
  def serializeJson(value: Any): String = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(value)

  private[this] def typeReference[T: Manifest] = new TypeReference[T] {}
}
