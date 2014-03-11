package com.newzly.phantom

import scala.reflect.runtime.universe._
import scala.util.control.NonFatal

class EarlyInit[T: TypeTag] {
  val mirror = runtimeMirror(this.getClass.getClassLoader)
  val reflection  = mirror.reflect(this)

  typeTag[T].tpe.members.filter(_.isModule).foreach(m => reflection.reflectModule(m.asModule).instance)
}