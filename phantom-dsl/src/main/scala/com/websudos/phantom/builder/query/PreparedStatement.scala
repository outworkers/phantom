package com.websudos.phantom.builder.query


sealed trait ParametricNode
final class ParametricValue[PVT, P <: ParametricNode] extends ParametricNode
final class PNil extends ParametricNode

sealed trait ParametricCondition[V]

/**
  * Prepared statement
 */
class PreparedStatement[P <: ParametricNode](args: Any*) {

  type **[PVT, P <: ParametricNode] = ParametricValue[PVT, P]

  def where[V](pc: ParametricCondition[V]): PreparedStatement[V ** P] = {
    new PreparedStatement[V ** P](args)
  }

  def withParams[V, PN <: ParametricNode](value: V)(implicit ev: P =:= V ** PN): PreparedStatement[PN] = {
    new PreparedStatement[PN](args :+ value)
  }

}
