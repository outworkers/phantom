package com.newzly.phantom

import java.lang.reflect.Method
import scala.collection.mutable.ListBuffer
import scala.language.existentials
import com.newzly.phantom.column.Column
import com.twitter.util.NonFatal

trait EarlyInit[T <: CassandraTable[T, R], R] {
  self: CassandraTable[T, R] =>

  protected[this] var fieldList: List[FieldHolder] = Nil
  protected[this] var fieldMap: Map[String, FieldHolder] = Map.empty

  def fieldOrder: List[Column[T, R, _]] = Nil

  protected[this] def isField(m: Method) = {
    !m.isSynthetic && classOf[Column[_, _, _]].isAssignableFrom(m.getReturnType)
  }

  protected[this] val rootClass = getClass.getSuperclass

  def introspect(rec: CassandraTable[T, R], methods: Array[Method])(f: (Method, Column[T, R, _]) => Any): Unit = {

    // find all the potential fields
    val potentialFields = methods.toList.filter(isField)
    potentialFields foreach {
      potential => {
        logger.info(s"Potential field ${potential.getName}")
      }
    }

    // any fields with duplicate names get put into a List
    val map: Map[String, List[Method]] = potentialFields.foldLeft[Map[String, List[Method]]](Map()) {
      case (map, method) => val name = method.getName
        map + (name -> (method :: map.getOrElse(name, Nil)))
    }

    // sort each list based on having the most specific type and use that method
    val realMeth = map.values.map(_.sortWith {
      case (a, b) => !a.getReturnType.isAssignableFrom(b.getReturnType)
    }).map(_.head)

    for (v <- realMeth) {
      v.invoke(rec) match {
        case mf: Column[T, R, _ ]  =>
          f(v, mf)
        case _ =>
      }
    }

  }

  val tArray = new ListBuffer[FieldHolder]

  val methods = rootClass.getMethods

  introspect(this, methods) {
    case (v, mf) => tArray += FieldHolder(mf.name, v, mf)
  }

  fieldList = {
    val ordered = fieldOrder.flatMap(f => tArray.find(_.metaField == f))
    ordered ++ (tArray -- ordered)
  }

  fieldMap = Map() ++ fieldList.map(i => (i.name, i))


  case class FieldHolder(name: String, method: Method, metaField: Column[T, R, _]) {
    def field(inst: CassandraTable[T, R]): Column[T, R, _] = {
      try{
        method.invoke(inst).asInstanceOf[Column[T, R, _]]
      } catch {
        case NonFatal(err) => println(s"${err.getMessage}"); throw err
      }
    }
  }
}