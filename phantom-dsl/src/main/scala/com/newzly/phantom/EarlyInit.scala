package com.newzly.phantom

import scala.reflect.runtime.universe._
import scala.util.control.NonFatal

trait EarlyInit {
  val mirror = runtimeMirror(this.getClass.getClassLoader)
  val reflection  = mirror.reflect(this)

  mirror
    .classSymbol(getClass)
    .toType
    .members
    .filter(_.isModule)
    .foreach(m => {
      val module = reflection.reflectModule(m.asModule)
        try {
          module.instance
        } catch {
          case NonFatal(err) =>
        }
    })
}