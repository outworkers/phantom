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

  /**
   * A first approach to defining parametric values. The issue is that it takes them in reverse order
   * @param value
   * @param ev
   * @tparam V
   * @tparam PN
   * @return
   */
  def withParam[V, PN <: ParametricNode](value: V)(implicit ev: P =:= V ** PN): PreparedStatement[PN] = {
    new PreparedStatement[PN](args :+ value)
  }


  /**
   * Second option is to define `withParams` methods each with different number of parameters in the chain.
   * @param v1
   * @param v2
   * @param v3
   * @param v4
   * @param ev
   * @tparam V1
   * @tparam V2
   * @tparam V3
   * @tparam V4
   * @return
   */
  def withParams[V1, V2, V3, V4](v1: V1, v2: V2, v3: V3, v4: V4)
                                (implicit ev: P =:= V4 ** V3 ** V2 ** V1 ** PNil): PreparedStatement[PNil] = {
    new PreparedStatement[PNil](v1, v2, v3, v4)
  }


  /**
   * It should be also possible to define parameter value one at once using the same technique.
   * @param value First parameter value
   * @param ev
   * @tparam V1
   * @tparam V2
   * @tparam V3
   * @tparam V4
   * @return
   */
  def withParams[V1, V2, V3, V4](value: V1)
                                (implicit ev: P =:= V4 ** V3 ** V2 ** V1 ** PNil): PreparedStatement[PNil] = {
    new PreparedStatement[PNil](value +: args)
  }

}
